package fuzs.bettermodsbutton.client.handler;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
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
    private static TitleScreenModUpdateIndicator pauseScreenUpdateIndicator;

    @SuppressWarnings("DataFlowIssue")
    public static void onScreen$Init$Post(final ScreenEvent.Init.Post evt) {
        Screen screen = evt.getScreen();
        // check for exact classes so we only apply to vanilla
        if (screen.getClass() == TitleScreen.class) {
            handleMainMenu(screen.getMinecraft(), screen, evt.getListenersList(), button -> {
                evt.addListener(button);
                TitleScreenModUpdateIndicator indicator = createModUpdateIndicator(screen, button);
                if (indicator != null) {
                    ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, indicator, "modUpdateNotification");
                }
            }, evt::removeListener);
        } else if (screen.getClass() == PauseScreen.class) {
            // vanilla's pause screen can be blank, so we don't want to add our button then
            if (ObfuscationReflectionHelper.<Boolean, PauseScreen>getPrivateValue(PauseScreen.class, (PauseScreen) screen, "f_96306_")) {
                handlePauseScreen(screen.getMinecraft(), screen, evt.getListenersList(), button -> {
                    evt.addListener(button);
                    pauseScreenUpdateIndicator = createModUpdateIndicator(screen, button);
                }, evt::removeListener);
            }
        }
    }

    @Nullable
    private static TitleScreenModUpdateIndicator createModUpdateIndicator(Screen screen, Button button) {
        if (ClientConfig.INSTANCE.updateNotification.get()) {
            TitleScreenModUpdateIndicator indicator = new TitleScreenModUpdateIndicator(button);
            indicator.resize(screen.getMinecraft(), screen.width, screen.height);
            indicator.init();
            return indicator;
        }
        return null;
    }

    public static void onScreen$Render(final ScreenEvent.Render evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (pauseScreenUpdateIndicator != null) {
                pauseScreenUpdateIndicator.render(evt.getPoseStack(), evt.getMouseX(), evt.getMouseY(), evt.getPartialTick());
            }
        }
    }

    public static void onScreen$Closing(final ScreenEvent.Closing evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            pauseScreenUpdateIndicator = null;
        }
    }

    private static void handleMainMenu(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.mainMenuMode.get() == ClientConfig.MainMenuMode.NO_CHANGE) return;
        ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) screen, new TitleScreenModUpdateIndicator(null), "modUpdateNotification");
        switch (ClientConfig.INSTANCE.mainMenuMode.get()) {
            case INSERT_BELOW_REALMS -> {
                Button modsButton = tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods", 200);
                if (modsButton != null) {
                    addListener.accept(modsButton);
                    moveButtonsUpAndDown(screen, children, modsButton.y + modsButton.getHeight());
                    modsButton.y += 24;
                    findButton(children, "menu.online").ifPresent(button -> {
                        button.setWidth(200);
                        button.x -= 102;
                    });
                    // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                    // field name: realmsNotificationsScreen
                    Screen realmsNotifications = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) screen, "f_96726_");
                    if (realmsNotifications != null) {
                        // height is only used for widget placement, it is divided by 4
                        realmsNotifications.height -= 48;
                    }
                }
            }
            case NONE -> {
                findButton(children, "fml.menu.mods").ifPresent(removeListener);
                findButton(children, "menu.online").ifPresent(button -> {
                    button.setWidth(200);
                    button.x -= 102;
                });
            }
            case LEFT_TO_REALMS -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods"));
            }
            case RIGHT_TO_REALMS -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.online"));
                findButton(children, "menu.online").ifPresent(button -> {
                    tryReplaceButton(children, removeListener, "fml.menu.mods", button, -1);
                    // field name: realmsNotificationsScreen
                    Screen realmsNotifications = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) screen, "f_96726_");
                    if (realmsNotifications != null) {
                        // width is only used for widget placement, it is divided by 2
                        realmsNotifications.width -= 204;
                    }
                });
            }
            case REPLACE_REALMS -> {
                findButton(children, "menu.online").ifPresent(removeListener);
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods", 200));
            }
        }
    }

    private static void handlePauseScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.pauseScreenMode.get() == ClientConfig.PauseScreenMode.NO_CHANGE) return;
        findButton(children, "fml.menu.mods").ifPresent(button -> {
            moveButtonsUpAndDown(screen, children, -12, button.y);
            removeListener.accept(button);
        });
        switch (ClientConfig.INSTANCE.pauseScreenMode.get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                Button modsButton = tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.options", 204);
                if (modsButton != null) {
                    addListener.accept(modsButton);
                    moveButtonsUpAndDown(screen, children, modsButton.y);
                    modsButton.y -= 24;
                }
            }
            case REPLACE_FEEDBACK -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "menu.sendFeedback"));
            }
            case REPLACE_BUGS -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "menu.reportBugs"));
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "menu.sendFeedback",204));
            }
            case REPLACE_AND_MOVE_LAN -> {
                findButton(children, "menu.sendFeedback").ifPresent(removeListener);
                findButton(children, "menu.reportBugs").ifPresent(removeListener);
                addListener.accept(tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.shareToLan"));
                findButton(children, "menu.shareToLan").ifPresent(button -> {
                    button.setWidth(204);
                    button.x -= 106;
                    button.y -= 24;
                });
            }
            case INSERT_AND_MOVE_LAN -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.shareToLan"));
                findButton(children, "menu.shareToLan").ifPresent(button -> {
                    moveButtonsUpAndDown(screen, children, button.x);
                    button.setWidth(204);
                    button.x -= 106;
                    button.y -= 24;
                });
            }
        }
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int splitAt) {
        moveButtonsUpAndDown(screen, listeners, 12, splitAt);
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int moveAmount, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof AbstractWidget element && !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
                if (splitAt <= element.y) {
                    element.y = element.y + moveAmount;
                } else {
                    element.y = element.y - moveAmount;
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

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s) {
        return tryReplaceButton(minecraft, screen, children, removeListener, s, -1);
    }

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s, int newWidth) {
        Button newButton = tryReplaceButton(children, removeListener, s, new Button(0, 0, 0, 0, CommonComponents.EMPTY, $ -> {
            minecraft.setScreen(new ModListScreen(screen));
        }), newWidth);
        if (newButton != null) {
            Component title = buildModsComponent(ClientConfig.INSTANCE.addModCount.get(), newButton.getWidth() < 200);
            newButton.setMessage(title);
        }
        return newButton;
    }

    @Nullable
    private static Button tryReplaceButton(List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s, Button newButton, int newWidth) {
        return findButton(children, s).map(button -> {
            removeListener.accept(button);
            newButton.x = button.x;
            newButton.y = button.y;
            newButton.setWidth(newWidth != -1 ? newWidth : button.getWidth());
            newButton.setHeight(button.getHeight());
            return newButton;
        }).orElse(null);
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
