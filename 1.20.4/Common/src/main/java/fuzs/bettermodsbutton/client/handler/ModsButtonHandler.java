package fuzs.bettermodsbutton.client.handler;

import fuzs.bettermodsbutton.ClientAbstractions;
import fuzs.bettermodsbutton.config.MainMenuMode;
import fuzs.bettermodsbutton.config.PauseScreenMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModsButtonHandler {

    public static void onAfterInitScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> list, Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
        // check for exact classes so we only apply to vanilla
        if (screen instanceof TitleScreen titleScreen && screen.getClass() == TitleScreen.class) {
            handleMainMenu(minecraft, titleScreen, list, button -> {
                add.accept(button);
                ClientAbstractions.setModUpdateNotification(screen, button);
            }, remove);
        } else if (screen instanceof PauseScreen pauseScreen && screen.getClass() == PauseScreen.class) {
            // vanilla's pause screen can be blank, so we don't want to add our button then
            if (pauseScreen.showsPauseMenu()) {
                handlePauseScreen(minecraft, pauseScreen, list, button -> {
                    add.accept(button);
                    ClientAbstractions.setModUpdateNotification(screen, button);
                }, remove);
            }
        }
    }

    private static void handleMainMenu(Minecraft minecraft, TitleScreen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientAbstractions.getClientConfig().getMainMenuMode().get() == MainMenuMode.NO_CHANGE) return;
        ClientAbstractions.setModUpdateNotification(screen, null);
        switch (ClientAbstractions.getClientConfig().getMainMenuMode().get()) {
            case INSERT_BELOW_REALMS -> {
                Button modsButton = tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods", 200);
                if (modsButton != null) {
                    addListener.accept(modsButton);
                    moveButtonsUpAndDown(screen, children, modsButton.getY() + modsButton.getHeight());
                    modsButton.setY(modsButton.getY() + 24);
                    findButton(children, "menu.online").ifPresent(button -> {
                        button.setWidth(200);
                        button.setX(button.getX() - 102);
                    });
                    // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                    if (screen.realmsNotificationsScreen != null) {
                        // height is only used for widget placement, it is divided by 4
                        screen.realmsNotificationsScreen.height -= 48;
                    }
                }
            }
            case NONE -> {
                findButton(children, "fml.menu.mods").ifPresent(removeListener);
                findButton(children, "menu.online").ifPresent(button -> {
                    button.setWidth(200);
                    button.setX(button.getX() - 102);
                });
            }
            case LEFT_TO_REALMS -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods"));
            }
            case RIGHT_TO_REALMS -> {
                addListener.accept(tryReplaceButton(minecraft, screen, children, $ -> {}, "menu.online"));
                findButton(children, "menu.online").ifPresent(button -> {
                    tryReplaceButton(children, removeListener, "fml.menu.mods", button, -1);
                    if (screen.realmsNotificationsScreen != null) {
                        // width is only used for widget placement, it is divided by 2
                        screen.realmsNotificationsScreen.width -= 204;
                    }
                });
            }
            case REPLACE_REALMS -> {
                findButton(children, "menu.online").ifPresent(removeListener);
                addListener.accept(tryReplaceButton(minecraft, screen, children, removeListener, "fml.menu.mods", 200));
            }
        }
    }

    private static void handlePauseScreen(Minecraft minecraft, PauseScreen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientAbstractions.getClientConfig().getPauseScreenMode().get() == PauseScreenMode.NO_CHANGE) return;
        findButton(children, "fml.menu.mods").ifPresent(button -> {
            moveButtonsUpAndDown(screen, children, -12, button.getY());
            removeListener.accept(button);
        });
        switch (ClientAbstractions.getClientConfig().getPauseScreenMode().get()) {
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
                    button.setX(button.getX() - 106);
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

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int moveAmount, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof LayoutElement element && !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
                if (splitAt <= element.getY()) {
                    element.setY(element.getY() + moveAmount);
                } else {
                    element.setY(element.getY() - moveAmount);
                }
            }
        }
    }

    private static boolean isElementTooCloseToScreenBorder(LayoutElement element, int screenWidth, int screenHeight) {
        ScreenRectangle rectangle = element.getRectangle();
        int safeArea = ClientAbstractions.getClientConfig().getSafeArea().get();
        screenWidth -= 2 * safeArea;
        screenHeight -= 2 * safeArea;
        ScreenRectangle intersection = rectangle.intersection(new ScreenRectangle(safeArea, safeArea, Math.max(0, screenWidth), Math.max(0, screenHeight)));
        return intersection == null || !intersection.equals(rectangle);
    }

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s) {
        return tryReplaceButton(minecraft, screen, children, removeListener, s, -1);
    }

    @Nullable
    private static Button tryReplaceButton(Minecraft minecraft, Screen screen, List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s, int newWidth) {
        Button newButton = tryReplaceButton(children, removeListener, s, Button.builder(CommonComponents.EMPTY, $ -> {
            minecraft.setScreen(ClientAbstractions.makeModListScreen(screen));
        }).build(), newWidth);
        if (newButton != null) {
            Component title = buildModsComponent(ClientAbstractions.getClientConfig().getAddModCount().get(), newButton.getWidth() < 200);
            newButton.setMessage(title);
        }
        return newButton;
    }

    @Nullable
    private static Button tryReplaceButton(List<GuiEventListener> children, Consumer<GuiEventListener> removeListener, String s, Button newButton, int newWidth) {
        return findButton(children, s).map(button -> {
            removeListener.accept(button);
            newButton.setPosition(button.getX(), button.getY());
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
            component = component.append(" ").append(Component.translatable(translationKey, ClientAbstractions.getModListSize()));
        }
        return component;
    }
}
