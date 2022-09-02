package fuzs.bettermodsbutton.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.mixin.client.accessor.PauseScreenAccessor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
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

public class ModScreenHandler {
    @Nullable
    private TitleScreenModUpdateIndicator modUpdateNotification;

    public void onScreen$Init$Post(final ScreenEvent.Init.Post evt) {
        // check for exact classes so we only apply to vanilla
        if (evt.getScreen().getClass() == TitleScreen.class) {
            this.handleMainMenu(evt);
        } else if (evt.getScreen().getClass() == PauseScreen.class) {
            // vanilla's pause screen can be blank, so we don't want to add our button then
            // was just checking if any buttons are present previously, but that would break together with other mods ignoring the empty screen, so we check the field directly instead
            if (((PauseScreenAccessor) evt.getScreen()).getShowPauseMenu()) {
                this.handlePauseScreen(evt);
            }
        }
    }

    public void onScreen$Render(final ScreenEvent.Render evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            // this will still be null if we are on an empty pause screen (from pressing F3 + Esc)
            if (this.modUpdateNotification != null) {
                this.modUpdateNotification.render(evt.getPoseStack(), evt.getMouseX(), evt.getMouseY(), evt.getPartialTick());
            }
        }
    }

    public void onScreen$Closing(final ScreenEvent.Closing evt) {
        if (evt.getScreen().getClass() == PauseScreen.class) {
            this.modUpdateNotification = null;
        }
    }

    private void handleMainMenu(ScreenEvent.Init evt) {
        if (ClientConfig.INSTANCE.mainMenuMode.get() == ClientConfig.MainMenuMode.NO_CHANGE) return;
        final boolean modCount = ClientConfig.INSTANCE.modCount.get();
        this.getButton(evt.getListenersList(), "fml.menu.mods").ifPresent(evt::removeListener);
        Button modsButton = null;
        switch (ClientConfig.INSTANCE.mainMenuMode.get()) {
            case INSERT_BELOW_REALMS -> {
                this.moveButtonsUpAndDown(evt.getListenersList(), evt.getScreen().height / 4 + 48 + 72 + 12);
                // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) evt.getScreen(), "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // height is only used for widget placement, it is divided by 4
                    realmsNotificationsScreen.height -= 48;
                }
                this.getButton(evt.getListenersList(), "menu.online").ifPresent(widget -> {
                    widget.setWidth(200);
                    widget.x = evt.getScreen().width / 2 - 100;
                });
                modsButton = new Button(evt.getScreen().width / 2 - 100, evt.getScreen().height / 4 + 48 + 24 * 3 - 12, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case NONE -> this.getButton(evt.getListenersList(), "menu.online").ifPresent(widget -> {
                widget.setWidth(200);
                widget.x = evt.getScreen().width / 2 - 100;
            });
            case LEFT_TO_REALMS -> modsButton = new Button(evt.getScreen().width / 2 - 100, evt.getScreen().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
            });
            case RIGHT_TO_REALMS -> {
                this.getButton(evt.getListenersList(), "menu.online").ifPresent(widget -> widget.x = evt.getScreen().width / 2 - 100);
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) evt.getScreen(), "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // width is only used for widget placement, it is divided by 2
                    realmsNotificationsScreen.width -= 204;
                }
                modsButton = new Button(evt.getScreen().width / 2 + 2, evt.getScreen().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case REPLACE_REALMS -> {
                this.getButton(evt.getListenersList(), "menu.online").ifPresent(evt::removeListener);
                // field name: realmsNotificationsScreen
                ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) evt.getScreen(), null, "f_96726_");
                modsButton = new Button(evt.getScreen().width / 2 - 100, evt.getScreen().height / 4 + 48 + 24 * 2, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
        }
        if (modsButton != null) evt.addListener(modsButton);
        ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) evt.getScreen(), TitleScreenModUpdateIndicator.init((TitleScreen) evt.getScreen(), ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null), "modUpdateNotification");
    }

    private void handlePauseScreen(ScreenEvent.Init evt) {
        if (ClientConfig.INSTANCE.pauseScreenMode.get() == ClientConfig.PauseScreenMode.NONE) return;
        final boolean modCount = ClientConfig.INSTANCE.modCount.get();
        Button modsButton = null;
        switch (ClientConfig.INSTANCE.pauseScreenMode.get()) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                this.moveButtonsUpAndDown(evt.getListenersList(), evt.getScreen().height / 4 + 96 - 16);
                modsButton = new Button(evt.getScreen().width / 2 - 102, evt.getScreen().height / 4 + 96 - 16 - 12, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case REPLACE_FEEDBACK -> {
                this.getButton(evt.getListenersList(), "menu.sendFeedback").ifPresent(evt::removeListener);
                modsButton = new Button(evt.getScreen().width / 2 - 102, evt.getScreen().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case REPLACE_BUGS -> {
                this.getButton(evt.getListenersList(), "menu.reportBugs").ifPresent(evt::removeListener);
                modsButton = new Button(evt.getScreen().width / 2 + 4, evt.getScreen().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                this.getButton(evt.getListenersList(), "menu.sendFeedback").ifPresent(evt::removeListener);
                this.getButton(evt.getListenersList(), "menu.reportBugs").ifPresent(evt::removeListener);
                modsButton = new Button(evt.getScreen().width / 2 - 102, evt.getScreen().height / 4 + 72 - 16, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case REPLACE_AND_MOVE_LAN -> {
                this.getButton(evt.getListenersList(), "menu.sendFeedback").ifPresent(evt::removeListener);
                this.getButton(evt.getListenersList(), "menu.reportBugs").ifPresent(evt::removeListener);
                this.getButton(evt.getListenersList(), "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = evt.getScreen().width / 2 - 102;
                    widget.y = evt.getScreen().height / 4 + 72 - 16;
                });
                modsButton = new Button(evt.getScreen().width / 2 + 4, evt.getScreen().height / 4 + 96 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
            case INSERT_AND_MOVE_LAN -> {
                this.moveButtonsUpAndDown(evt.getListenersList(), evt.getScreen().height / 4 + 96 - 16);
                this.getButton(evt.getListenersList(), "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = evt.getScreen().width / 2 - 102;
                    widget.y = evt.getScreen().height / 4 + 96 - 16 - 12;
                });
                modsButton = new Button(evt.getScreen().width / 2 + 4, evt.getScreen().height / 4 + 96 - 16 + 12, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getScreen().getMinecraft().setScreen(createModListScreen(evt.getScreen()));
                });
            }
        }
        if (modsButton != null) evt.addListener(modsButton);
        this.modUpdateNotification = new TitleScreenModUpdateIndicator(ClientConfig.INSTANCE.updateNotification.get() ? modsButton : null);
        this.modUpdateNotification.resize(evt.getScreen().getMinecraft(), evt.getScreen().width, evt.getScreen().height);
        this.modUpdateNotification.init();
    }

    private void moveButtonsUpAndDown(List<GuiEventListener> listeners, int splitAt) {
        for (GuiEventListener widget : listeners) {
            // plain text button is only used for copyright text on title screen, really shouldn't accidentally remove that (again...)
            if (widget instanceof Button button && !(button instanceof PlainTextButton))
                if (splitAt <= button.y) {
                    button.y += 12;
                } else {
                    button.y -= 12;
                }
        }
    }

    private Optional<Button> getButton(List<GuiEventListener> widgets, String s) {
        for (GuiEventListener widget : widgets) {
            if (widget instanceof Button button && this.containsKey(button, s)) {
                return Optional.of(button);
            }
        }
        return Optional.empty();
    }

    private boolean containsKey(Button button, String key) {
        final ComponentContents message = button.getMessage().getContents();
        return message instanceof TranslatableContents contents && contents.getKey().equals(key);
    }

    private Component getModsComponent(boolean withCount, boolean compact) {
        MutableComponent component = Component.translatable("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(Component.translatable(translationKey, ModList.get().size()));
        }
        return component;
    }
    
    private static Screen createModListScreen(Screen lastScreen) {
        if (!ClientConfig.INSTANCE.forceDirtBackground.get()) {
            return new ModListScreen(lastScreen);
        }
        return new ModListScreen(lastScreen) {
            @Override
            public void renderBackground(PoseStack pMatrixStack, int pVOffset) {
                this.renderDirtBackground(pVOffset);
            }
        };
    }
}
