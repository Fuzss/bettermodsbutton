package fuzs.bettermodsbutton.client.handler;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModsButtonHandler {
    @Nullable
    private static TitleScreenModUpdateIndicator modUpdateNotification;

    @SuppressWarnings("DataFlowIssue")
    public static void onScreen$Init$Post(final ScreenEvent.Init.Post evt) {
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

    public static void onScreen$Render(final ScreenEvent.Render evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (modUpdateNotification != null) {
                modUpdateNotification.render(evt.getGuiGraphics(), evt.getMouseX(), evt.getMouseY(), evt.getPartialTick());
            }
        }
    }

    public static void onScreen$Closing(final ScreenEvent.Closing evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
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
                    widget.setX(screen.width / 2 - 100);
                });
                modsButton = Button.builder(buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 3 - 12, 200, 20).build();
            }
            case NONE -> findButton(children, "menu.online").ifPresent(widget -> {
                widget.setWidth(200);
                widget.setX(screen.width / 2 - 100);
            });
            case LEFT_TO_REALMS -> modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                minecraft.setScreen(new ModListScreen(screen));
            }).bounds(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 2, 98, 20).build();
            case RIGHT_TO_REALMS -> {
                findButton(children, "menu.online").ifPresent(widget -> widget.setX(screen.width / 2 - 100));
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) screen, "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // width is only used for widget placement, it is divided by 2
                    realmsNotificationsScreen.width -= 204;
                }
                modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 + 2, screen.height / 4 + 48 + 24 * 2, 98, 20).build();
            }
            case REPLACE_REALMS -> {
                findButton(children, "menu.online").ifPresent(removeListener);
                // field name: realmsNotificationsScreen
                ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, null, "f_96726_");
                modsButton = Button.builder(buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 - 100, screen.height / 4 + 48 + 24 * 2, 200, 20).build();
            }
        }
        if (modsButton != null) addListener.accept(modsButton);
        ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, TitleScreenModUpdateIndicator.init((TitleScreen) screen, ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null), "modUpdateNotification");
    }

    private static void handlePauseScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.pauseScreenMode.get() == ClientConfig.PauseScreenMode.NONE) return;
        final boolean modCount = ClientConfig.INSTANCE.modCount.get();
        Button modsButton = null;
        switch (ClientConfig.INSTANCE.pauseScreenMode.get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                moveButtonsUpAndDown(screen, children, screen.height / 4 + 96 - 16);
                modsButton = Button.builder(buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 - 102, screen.height / 4 + 96 - 16 - 12, 204, 20).build();
            }
            case REPLACE_FEEDBACK -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 - 102, screen.height / 4 + 72 - 16, 98, 20).build();
            }
            case REPLACE_BUGS -> {
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 + 4, screen.height / 4 + 72 - 16, 98, 20).build();
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                modsButton = Button.builder(buildModsComponent(modCount, false), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 - 102, screen.height / 4 + 72 - 16, 204, 20).build();
            }
            case REPLACE_AND_MOVE_LAN -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                findButton(children, "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.setX(screen.width / 2 - 102);
                    widget.setY(screen.height / 4 + 72 - 16);
                });
                modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 + 4, screen.height / 4 + 96 - 16, 98, 20).build();
            }
            case INSERT_AND_MOVE_LAN -> {
                moveButtonsUpAndDown(screen, children, screen.height / 4 + 96 - 16);
                findButton(children, "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.setX(screen.width / 2 - 102);
                    widget.setY(screen.height / 4 + 96 - 16 - 12);
                });
                modsButton = Button.builder(buildModsComponent(modCount, true), button -> {
                    minecraft.setScreen(new ModListScreen(screen));
                }).bounds(screen.width / 2 + 4, screen.height / 4 + 96 - 16 + 12, 98, 20).build();
            }
        }
        if (modsButton != null) addListener.accept(modsButton);
        modUpdateNotification = new TitleScreenModUpdateIndicator(ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null);
        modUpdateNotification.resize(minecraft, screen.width, screen.height);
        modUpdateNotification.init();
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof LayoutElement element && !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
                if (splitAt <= element.getY()) {
                    element.setY(element.getY() + 12);
                } else {
                    element.setY(element.getY() - 12);
                }
            }
        }
    }

    private static boolean isElementTooCloseToScreenBorder(LayoutElement element, int screenWidth, int screenHeight) {
        ScreenRectangle rectangle = element.getRectangle();
        int safeArea = ClientConfig.INSTANCE.safeArea.get();
        screenWidth -= 2 * safeArea;
        screenHeight -= 2 * safeArea;
        ScreenRectangle intersection = rectangle.intersection(new ScreenRectangle(safeArea, safeArea, Math.max(0, screenWidth), Math.max(0, screenHeight)));
        return intersection == null || !intersection.equals(rectangle);
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
        final ComponentContents message = button.getMessage().getContents();
        return message instanceof TranslatableContents contents && contents.getKey().equals(key);
    }

    private static Component buildModsComponent(boolean withCount, boolean compact) {
        MutableComponent component = Component.translatable("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(Component.translatable(translationKey, ModList.get().size()));
        }
        return component;
    }
}
