package fuzs.bettermodsbutton.client.handler;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModsButtonHandler {
    @Nullable
    private static NotificationModUpdateScreen modUpdateNotification;

    @SuppressWarnings("DataFlowIssue")
    public static void onScreen$Init$Post(final ScreenEvent.InitScreenEvent.Post evt) {
        // check for exact classes so we only apply to vanilla
        if (evt.getScreen().getClass() == TitleScreen.class) {
            handleMainMenu(evt.getScreen().getMinecraft(), evt.getScreen(), evt.getListenersList(), evt::addListener, evt::removeListener);
            // vanilla's pause screen can be blank, so we don't want to add our button then
        } else if (evt.getScreen().getClass() == PauseScreen.class) {
            if (ObfuscationReflectionHelper.<Boolean, PauseScreen>getPrivateValue(PauseScreen.class, (PauseScreen) evt.getScreen(), "f_96306_")) {
                handlePauseScreen(evt.getScreen().getMinecraft(), evt.getScreen(), evt.getListenersList(), evt::addListener, evt::removeListener);
            }
        }
    }

    public static void onScreen$Render(final ScreenEvent.DrawScreenEvent evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (modUpdateNotification != null) {
                modUpdateNotification.render(evt.getPoseStack(), evt.getMouseX(), evt.getMouseY(), evt.getPartialTicks());
            }
        }
    }

    public static void onScreen$Closing(final ScreenOpenEvent evt) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null && minecraft.screen.getClass() == PauseScreen.class) {
            modUpdateNotification = null;
        }
    }

    private static void handleMainMenu(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.mainMenuMode.get() == ClientConfig.MainMenuMode.NO_CHANGE) return;
        final boolean modCount = ClientConfig.INSTANCE.modCount.get();
        findButton(children, "fml.menu.mods").ifPresent(removeListener);
        Button modsButton = null;
        switch (ClientConfig.INSTANCE.mainMenuMode.get()) {
            case INSERT_BELOW_REALMS -> {
                moveButtonsUpAndDown(screen, children, screen.height / 4 + 48 + 72 + 12);
                // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) screen, "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // height is only used for widget placement, it is divided by 4
                    realmsNotificationsScreen.height -= 48;
                }
                findButton(children, "menu.online").ifPresent(widget -> {
                    widget.setWidth(200);
                    widget.x = screen.width / 2 - 100;
                });
                modsButton = new Button(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 3 - 12, 200, 20, buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case NONE -> findButton(children, "menu.online").ifPresent(widget -> {
                widget.setWidth(200);
                widget.x = screen.width / 2 - 100;
            });
            case LEFT_TO_REALMS -> modsButton = new Button(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 2, 98, 20, buildModsComponent(modCount, true), button -> {
                minecraft.setScreen(new ModListScreen(screen));
            });
            case RIGHT_TO_REALMS -> {
                findButton(children, "menu.online").ifPresent(widget -> widget.x = screen.width / 2 - 100);
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) screen, "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // width is only used for widget placement, it is divided by 2
                    realmsNotificationsScreen.width -= 204;
                }
                modsButton = new Button(screen.width / 2 + 2, screen.height / 4 + 48 + 24 * 2, 98, 20, buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case REPLACE_REALMS -> {
                findButton(children, "menu.online").ifPresent(removeListener);
                // field name: realmsNotificationsScreen
                ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, null, "f_96726_");
                modsButton = new Button(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 2, 200, 20, buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
        }
        if (modsButton != null) addListener.accept(modsButton);
        ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, NotificationModUpdateScreen.init((TitleScreen) screen, ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null), "modUpdateNotification");
    }

    private static void handlePauseScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.pauseScreenMode.get() == ClientConfig.PauseScreenMode.NONE) return;
        final boolean modCount = ClientConfig.INSTANCE.modCount.get();
        Button modsButton = null;
        switch (ClientConfig.INSTANCE.pauseScreenMode.get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                moveButtonsUpAndDown(screen, children, screen.height / 4 + 96 - 16);
                modsButton = new Button(screen.width / 2 - 102, screen.height / 4 + 96 - 16 - 12, 204, 20, buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case REPLACE_FEEDBACK -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                modsButton = new Button(screen.width / 2 - 102, screen.height / 4 + 72 - 16, 98, 20, buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case REPLACE_BUGS -> {
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                modsButton = new Button(screen.width / 2 + 4, screen.height / 4 + 72 - 16, 98, 20, buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                modsButton = new Button(screen.width / 2 - 102, screen.height / 4 + 72 - 16, 204, 20, buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case REPLACE_AND_MOVE_LAN -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                findButton(children, "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = screen.width / 2 - 102;
                    widget.y = screen.height / 4 + 72 - 16;
                });
                modsButton = new Button(screen.width / 2 + 4, screen.height / 4 + 96 - 16, 98, 20, buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
            case INSERT_AND_MOVE_LAN -> {
                moveButtonsUpAndDown(screen, children, screen.height / 4 + 96 - 16);
                findButton(children, "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = screen.width / 2 - 102;
                    widget.y = screen.height / 4 + 96 - 16 - 12;
                });
                modsButton = new Button(screen.width / 2 + 4, screen.height / 4 + 96 - 16 + 12, 98, 20, buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                });
            }
        }
        if (modsButton != null) addListener.accept(modsButton);
        modUpdateNotification = new NotificationModUpdateScreen(ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null);
        modUpdateNotification.resize(minecraft, screen.width, screen.height);
        modUpdateNotification.init();
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof AbstractWidget element && !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
                if (splitAt <= element.y) {
                    element.y = element.y + 12;
                } else {
                    element.y = element.y - 12;
                }
            }
        }
    }

    private static boolean isElementTooCloseToScreenBorder(AbstractWidget abstractWidget, int screenWidth, int screenHeight) {
        int safeArea = ClientConfig.INSTANCE.safeArea.get();
        screenWidth -= 2 * safeArea;
        screenHeight -= 2 * safeArea;
        int i = Math.max(abstractWidget.x, safeArea);
        int j = Math.max(abstractWidget.y, safeArea);
        int k = Math.min(abstractWidget.x + abstractWidget.getWidth(), safeArea + Math.max(0, screenWidth));
        int l = Math.min(abstractWidget.y + abstractWidget.getHeight(), safeArea + Math.max(0, screenHeight));
        return i >= k || j >= l;
    }

    private static Optional<Button> findButton(List<GuiEventListener> widgets, String s) {
        for (GuiEventListener widget : widgets) {
            if (widget instanceof Button button && matchesTranslationKey(button, s)) {
                return Optional.of(button);
            }
        }
        return Optional.empty();
    }

    private static boolean matchesTranslationKey(Button button, String key) {
        final Component message = button.getMessage();
        return message instanceof TranslatableComponent contents && contents.getKey().equals(key);
    }

    private static Component buildModsComponent(boolean withCount, boolean compact) {
        MutableComponent component = new TranslatableComponent("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(new TranslatableComponent(translationKey, ModList.get().size()));
        }
        return component;
    }
}
