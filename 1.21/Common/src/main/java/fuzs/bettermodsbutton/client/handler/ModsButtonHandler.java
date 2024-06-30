package fuzs.bettermodsbutton.client.handler;

import fuzs.bettermodsbutton.config.MainMenuMode;
import fuzs.bettermodsbutton.config.PauseScreenMode;
import fuzs.bettermodsbutton.service.ClientAbstractions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ModsButtonHandler {
    public static final String KEY_MODS_COUNT = "button.mods.count";
    public static final String KEY_MODS_COUNT_COMPACT = "button.mods.count.compact";

    public static void onAfterInitScreen(Minecraft minecraft, Screen screen, List<GuiEventListener> list, Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
        // check for exact classes so we only apply to vanilla
        if (screen instanceof TitleScreen titleScreen && screen.getClass() == TitleScreen.class) {
            handleMainMenu(minecraft, titleScreen, list, add::accept, remove);
        } else if (screen instanceof PauseScreen pauseScreen && screen.getClass() == PauseScreen.class) {
            // vanilla's pause screen can be blank, so we don't want to add our button then
            if (pauseScreen.showsPauseMenu()) {
                handlePauseScreen(minecraft, pauseScreen, list, add::accept, remove);
            }
        }
    }

    private static void handleMainMenu(Minecraft minecraft, TitleScreen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientAbstractions.INSTANCE.getClientConfig().getMainMenuMode().get() == MainMenuMode.NO_CHANGE) {
            return;
        }

        Button modsButton = findButton(children, "fml.menu.mods");
        Button newModsButton = ClientAbstractions.INSTANCE.getNewModsButton(screen, modsButton);
        if (modsButton != newModsButton) {
            removeListener.accept(modsButton);
            addListener.accept(newModsButton);
            modsButton = newModsButton;
        }

        Button realmsButton = findButton(children, "menu.online");
        int fallbackRealmsButtonY = screen.height / 4 + 48 + 24 * 2;

        switch (ClientAbstractions.INSTANCE.getClientConfig().getMainMenuMode().get()) {
            case INSERT_BELOW_REALMS -> {
                moveButtonsUpAndDown(screen, children, modsButton.getY() + modsButton.getHeight());
                modsButton.setWidth(200);
                modsButton.setX(screen.width / 2 - 100);
                if (realmsButton != null) {
                    modsButton.setY(realmsButton.getY() + 24);
                    realmsButton.setWidth(200);
                    realmsButton.setX(screen.width / 2 - 100);
                    // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                    if (screen.realmsNotificationsScreen != null) {
                        // height is only used for widget placement, it is divided by 4
                        screen.realmsNotificationsScreen.height -= 48;
                    }
                } else {
                    modsButton.setY(fallbackRealmsButtonY + 24);
                }
            }
            case LEFT_TO_REALMS -> {
                modsButton.setWidth(98);
                modsButton.setX(screen.width / 2 - 100);
                if (realmsButton != null) {
                    modsButton.setY(realmsButton.getY());
                    realmsButton.setWidth(98);
                    realmsButton.setX(screen.width / 2 + 2);
                } else {
                    modsButton.setY(fallbackRealmsButtonY);
                }
            }
            case RIGHT_TO_REALMS -> {
                modsButton.setWidth(98);
                modsButton.setX(screen.width / 2 + 2);
                if (realmsButton != null) {
                    modsButton.setY(realmsButton.getY());
                    realmsButton.setWidth(98);
                    realmsButton.setX(screen.width / 2 - 100);
                    if (screen.realmsNotificationsScreen != null) {
                        // width is only used for widget placement, it is divided by 2
                        screen.realmsNotificationsScreen.width -= 204;
                    }
                } else {
                    modsButton.setY(fallbackRealmsButtonY);
                }
            }
            case REPLACE_REALMS -> {
                if (realmsButton != null) {
                    copyButtonProperties(realmsButton, modsButton);
                    modsButton.setWidth(200);
                    modsButton.setX(screen.width / 2 - 100);
                    removeListener.accept(realmsButton);
                    if (screen.realmsNotificationsScreen != null) {
                        screen.realmsNotificationsScreen = null;
                    }
                } else {
                    initButtonProperties(modsButton, screen.width / 2 - 100, fallbackRealmsButtonY, 200, 20);
                }
            }
            case NONE -> {
                removeListener.accept(modsButton);
                if (realmsButton != null) {
                    realmsButton.setWidth(200);
                    realmsButton.setX(screen.width / 2 - 100);
                }
            }
        }

        setModsButtonComponent(modsButton);
    }

    private static void handlePauseScreen(Minecraft minecraft, PauseScreen screen, List<GuiEventListener> children, Consumer<Button> addListener, Consumer<GuiEventListener> removeListener) {
        if (ClientAbstractions.INSTANCE.getClientConfig().getPauseScreenMode().get() == PauseScreenMode.NO_CHANGE) {
            return;
        }

        Button modsButton = findButton(children, "fml.menu.mods");
        if (modsButton != null) {
            moveButtonsUpAndDown(screen, children, -12, modsButton.getY());
        }
        Button newModsButton = ClientAbstractions.INSTANCE.getNewModsButton(screen, modsButton);
        if (modsButton != newModsButton) {
            removeListener.accept(modsButton);
            addListener.accept(newModsButton);
            modsButton = newModsButton;
        }

        switch (ClientAbstractions.INSTANCE.getClientConfig().getPauseScreenMode().get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                Button feedbackButton = findButton(children, "menu.sendFeedback");
                if (feedbackButton != null) {
                    copyButtonProperties(feedbackButton, modsButton);
                    moveButtonsUpAndDown(screen, children, feedbackButton.getY() + feedbackButton.getHeight());
                    modsButton.setWidth(204);
                    modsButton.setY(modsButton.getY() + 24);
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case REPLACE_FEEDBACK -> {
                Button feedbackButton = findButton(children, "menu.sendFeedback");
                if (feedbackButton != null) {
                    copyButtonProperties(feedbackButton, modsButton);
                    removeListener.accept(feedbackButton);
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case REPLACE_BUGS -> {
                Button bugsButton = findButton(children, "menu.reportBugs");
                if (bugsButton != null) {
                    copyButtonProperties(bugsButton, modsButton);
                    removeListener.accept(bugsButton);
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                Button feedbackButton = findButton(children, "menu.sendFeedback");
                Button bugsButton = findButton(children, "menu.reportBugs");
                if (feedbackButton != null && bugsButton != null) {
                    copyButtonProperties(feedbackButton, modsButton);
                    modsButton.setWidth(204);
                    removeListener.accept(feedbackButton);
                    removeListener.accept(bugsButton);
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case REPLACE_AND_MOVE_LAN -> {
                Button shareToLanButton = findButton(children, "menu.shareToLan");
                if (shareToLanButton == null) {
                    shareToLanButton = findButton(children, "menu.playerReporting");
                }
                if (shareToLanButton != null) {
                    copyButtonProperties(shareToLanButton, modsButton);
                    Button feedbackButton = findButton(children, "menu.sendFeedback");
                    Button bugsButton = findButton(children, "menu.reportBugs");
                    if (feedbackButton != null && bugsButton != null) {
                        copyButtonProperties(feedbackButton, shareToLanButton);
                        shareToLanButton.setWidth(204);
                        removeListener.accept(feedbackButton);
                        removeListener.accept(bugsButton);
                    } else {
                        removeListener.accept(shareToLanButton);
                    }
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case INSERT_AND_MOVE_LAN -> {
                Button shareToLanButton = findButton(children, "menu.shareToLan");
                if (shareToLanButton == null) {
                    shareToLanButton = findButton(children, "menu.playerReporting");
                }
                if (shareToLanButton != null) {
                    copyButtonProperties(shareToLanButton, modsButton);
                    Button feedbackButton = findButton(children, "menu.sendFeedback");
                    if (feedbackButton != null) {
                        moveButtonsUpAndDown(screen, children, shareToLanButton.getY());
                        copyButtonProperties(feedbackButton, shareToLanButton);
                        shareToLanButton.setWidth(204);
                        shareToLanButton.setY(shareToLanButton.getY() + 24);
                    } else {
                        removeListener.accept(shareToLanButton);
                    }
                } else {
                    removeListener.accept(modsButton);
                }
            }
            case NONE -> {
                removeListener.accept(modsButton);
            }
        }

        setModsButtonComponent(modsButton);
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int splitAt) {
        moveButtonsUpAndDown(screen, listeners, 12, splitAt);
    }

    private static void moveButtonsUpAndDown(Screen screen, List<GuiEventListener> listeners, int moveAmount, int splitAt) {
        for (GuiEventListener widget : listeners) {
            if (widget instanceof LayoutElement element &&
                    !isElementTooCloseToScreenBorder(element, screen.width, screen.height)) {
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
        int safeArea = ClientAbstractions.INSTANCE.getClientConfig().getSafeArea().get();
        screenWidth -= 2 * safeArea;
        screenHeight -= 2 * safeArea;
        ScreenRectangle intersection = rectangle.intersection(new ScreenRectangle(safeArea,
                safeArea,
                Math.max(0, screenWidth),
                Math.max(0, screenHeight)
        ));

        return intersection == null || !intersection.equals(rectangle);
    }

    private static void copyButtonProperties(Button from, Button to) {
        initButtonProperties(to, from.getX(), from.getY(), from.getWidth(), from.getHeight());
    }

    private static void initButtonProperties(Button button, int x, int y, int width, int height) {
        button.setPosition(x, y);
        button.setWidth(width);
        button.setHeight(height);
    }

    @Nullable
    private static Button findButton(List<GuiEventListener> widgets, String translationKey) {
        for (GuiEventListener widget : widgets) {
            if (widget instanceof Button button) {
                if (button.getMessage().getContents() instanceof TranslatableContents contents &&
                        contents.getKey().equals(translationKey)) {
                    return button;
                }
            }
        }

        return null;
    }

    private static void setModsButtonComponent(Button modsButton) {
        boolean includeCount = ClientAbstractions.INSTANCE.getClientConfig().getAddModCount().get();
        Component title = getModsComponent(includeCount, modsButton.getWidth() < 200);
        modsButton.setMessage(title);
    }

    private static Component getModsComponent(boolean includeCount, boolean isCompact) {
        MutableComponent component = Component.translatable("fml.menu.mods");
        if (includeCount) {
            String translationKey = isCompact ? KEY_MODS_COUNT_COMPACT : KEY_MODS_COUNT;
            component = component.append(" ")
                    .append(Component.translatable(translationKey, ClientAbstractions.INSTANCE.getModListSize()));
        }
        return component;
    }
}
