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
    private static TitleScreenModUpdateIndicator pauseScreenUpdateIndicator;

    @SuppressWarnings("DataFlowIssue")
    public static void onScreen$Init$Post(final ScreenEvent.Init.Post evt) {
        Screen screen = evt.getScreen();
        // check for exact classes so we only apply to vanilla
        if (screen.getClass() == TitleScreen.class) {
            handleMainMenu(screen.getMinecraft(), screen, evt.getListenersList(), evt::addListener, evt::removeListener);
            // vanilla's pause screen can be blank, so we don't want to add our button then
        } else if (screen.getClass() == PauseScreen.class) {
            if (ObfuscationReflectionHelper.<Boolean, PauseScreen>getPrivateValue(PauseScreen.class, (PauseScreen) screen, "f_96306_")) {
                handlePauseScreen(screen.getMinecraft(), screen, evt.getListenersList(), button -> {
                    evt.addListener(button);
                    if (ClientConfig.INSTANCE.updateNotification.get()) {
                        pauseScreenUpdateIndicator = new TitleScreenModUpdateIndicator(button);
                        pauseScreenUpdateIndicator.resize(screen.getMinecraft(), screen.width, screen.height);
                        pauseScreenUpdateIndicator.init();
                    }
                }, evt::removeListener);
            }
        }
    }

    public static void onScreen$Render(final ScreenEvent.Render evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (pauseScreenUpdateIndicator != null) {
                pauseScreenUpdateIndicator.render(evt.getGuiGraphics(), evt.getMouseX(), evt.getMouseY(), evt.getPartialTick());
            }
        }
    }

    public static void onScreen$Closing(final ScreenEvent.Closing evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            pauseScreenUpdateIndicator = null;
        }
    }

    private static void handleMainMenu(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.mainMenuMode.get() == ClientConfig.MainMenuMode.NO_CHANGE) return;
        final boolean modCount = ClientConfig.INSTANCE.addModCount.get();
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

    private static void handlePauseScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientConfig.INSTANCE.pauseScreenMode.get() == ClientConfig.PauseScreenMode.NO_CHANGE) return;
        findButton(children, "fml.menu.mods").ifPresent(button -> {
            moveButtonsUpAndDown(screen, children, -12, button.getY());
            removeListener.accept(button);
        });
        switch (ClientConfig.INSTANCE.pauseScreenMode.get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                Button modsButton = tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.options", 204);
                if (modsButton != null) {
                    addListener.accept(modsButton);
                    moveButtonsUpAndDown(screen, children, modsButton.getY());
                    modsButton.setY(modsButton.getY() - 24);
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
                    button.setX(button.getX() - 108);
                    button.setY(button.getY() - 24);
                });
            }
            case INSERT_AND_MOVE_LAN -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.shareToLan"));
                findButton(children, "menu.shareToLan").ifPresent(button -> {
                    moveButtonsUpAndDown(screen, children, button.getY());
                    button.setWidth(204);
                    button.setX(button.getX() - 106);
                    button.setY(button.getY() - 24);
                });
            }
        }
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int splitAt) {
        moveButtonsUpAndDown(screen, listeners, 12, splitAt);
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int amount, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof LayoutElement element && !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
                if (splitAt <= element.getY()) {
                    element.setY(element.getY() + amount);
                } else {
                    element.setY(element.getY() - amount);
                }
            }
        }
    }

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s) {
        return tryReplaceButton(minecraft, screen, children, removeListener, s, -1);
    }

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s, int newWidth) {
        return findButton(children, s).map(button -> {
            removeListener.accept(button);
            int width = newWidth != -1 ? newWidth : button.getWidth();
            Component title = buildModsComponent(ClientConfig.INSTANCE.addModCount.get(), width < 204);
            return Button.builder(title, $ -> {
                minecraft.setScreen(new ModListScreen(screen));
            }).bounds(button.getX(), button.getY(), width, button.getHeight()).build();
        }).orElse(null);
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
