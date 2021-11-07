package fuzs.bettermodsbutton.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.compat.catalogue.CatalogueModListFactory;
import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.lib.core.ModLoaderEnvironment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.gui.screen.ModListScreen;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("CodeBlock2Expr")
public class ModScreenHandler {
    private NotificationModUpdateScreen gameMenuNotification;

    @SubscribeEvent
    public void onInitGui(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui().getClass() == TitleScreen.class) {
            this.handleMainMenu(evt);
        } else if (evt.getGui() instanceof PauseScreen) {
            this.handlePauseScreen(evt);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(final GuiScreenEvent.DrawScreenEvent evt) {
        if (evt.getGui() instanceof PauseScreen) {
            this.gameMenuNotification.render(evt.getMatrixStack(), evt.getMouseX(), evt.getMouseY(), evt.getRenderPartialTicks());
        }
    }

    private void handleMainMenu(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (BetterModsButton.CONFIG.client().mainMenuMode == ClientConfig.MainMenuMode.NO_CHANGE) return;
        final boolean modCount = BetterModsButton.CONFIG.client().modCount;
        this.getButton(evt.getWidgetList(), "fml.menu.mods").ifPresent(evt::removeWidget);
        Button modsButton = null;
        switch (BetterModsButton.CONFIG.client().mainMenuMode) {
            case INSERT_BELOW_REALMS -> {
                for (GuiEventListener widget : evt.getWidgetList()) {
                    if (widget instanceof Button button)
                        if (evt.getGui().height / 4 + 48 + 72 + 12 <= button.y) {
                            button.y += 12;
                        } else {
                            button.y -= 12;
                        }
                }
                // move realms notification widget up by 12 pixels as the button itself, seems to be the easiest way without having to rewrite code
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) evt.getGui(), "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // height is only used for widget placement, it is divided by 4
                    realmsNotificationsScreen.height -= 48;
                }
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> {
                    widget.setWidth(200);
                    widget.x = evt.getGui().width / 2 - 100;
                });
                modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 3 - 12, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case NONE -> this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> {
                widget.setWidth(200);
                widget.x = evt.getGui().width / 2 - 100;
            });
            case LEFT_TO_REALMS -> modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
            });
            case RIGHT_TO_REALMS -> {
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> widget.x = evt.getGui().width / 2 - 100);
                // field name: realmsNotificationsScreen
                final Screen realmsNotificationsScreen = ObfuscationReflectionHelper.getPrivateValue(TitleScreen.class, (TitleScreen) evt.getGui(), "f_96726_");
                if (realmsNotificationsScreen != null) {
                    // width is only used for widget placement, it is divided by 2
                    realmsNotificationsScreen.width -= 204;
                }
                modsButton = new Button(evt.getGui().width / 2 + 2, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case REPLACE_REALMS -> {
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(evt::removeWidget);
                // field name: realmsNotificationsScreen
                ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) evt.getGui(), null, "f_96726_");
                modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
        }
        if (modsButton != null) evt.addWidget(modsButton);
        ObfuscationReflectionHelper.setPrivateValue(TitleScreen.class, (TitleScreen) evt.getGui(), NotificationModUpdateScreen.init((TitleScreen) evt.getGui(), BetterModsButton.CONFIG.client().updateNotification ? modsButton : null), "modUpdateNotification");
    }

    private void handlePauseScreen(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (BetterModsButton.CONFIG.client().pauseScreenMode == ClientConfig.PauseScreenMode.NONE) return;
        final boolean modCount = BetterModsButton.CONFIG.client().modCount;
        Button modsButton = null;
        switch (BetterModsButton.CONFIG.client().pauseScreenMode) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS -> {
                for (GuiEventListener widget : evt.getWidgetList()) {
                    if (widget instanceof Button button)
                        if (evt.getGui().height / 4 + 96 - 16 <= button.y) {
                            button.y += 12;
                        } else {
                            button.y -= 12;
                        }
                }
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 96 - 16 - 12, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case REPLACE_FEEDBACK -> {
                this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case REPLACE_BUGS -> {
                this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case REPLACE_FEEDBACK_AND_BUGS -> {
                this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 - 16, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case REPLACE_AND_MOVE_LAN -> {
                this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                this.getButton(evt.getWidgetList(), "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = evt.getGui().width / 2 - 102;
                    widget.y = evt.getGui().height / 4 + 72 - 16;
                });
                modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 96 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
            case INSERT_AND_MOVE_LAN -> {
                for (GuiEventListener widget : evt.getWidgetList()) {
                    if (widget instanceof Button button)
                        if (evt.getGui().height / 4 + 96 - 16 <= button.y) {
                            button.y += 12;
                        } else {
                            button.y -= 12;
                        }
                }
                this.getButton(evt.getWidgetList(), "menu.shareToLan").ifPresent(widget -> {
                    widget.setWidth(204);
                    widget.x = evt.getGui().width / 2 - 102;
                    widget.y = evt.getGui().height / 4 + 96 - 16 - 12;
                });
                modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 96 - 16 + 12, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
            }
        }
        if (modsButton != null) evt.addWidget(modsButton);
        this.gameMenuNotification = new NotificationModUpdateScreen(BetterModsButton.CONFIG.client().updateNotification ? modsButton : null);
        this.gameMenuNotification.resize(evt.getGui().getMinecraft(), evt.getGui().width, evt.getGui().height);
        this.gameMenuNotification.init();
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
        final Component message = button.getMessage();
        return message instanceof TranslatableComponent && ((TranslatableComponent) message).getKey().equals(key);
    }

    private Component getModsComponent(boolean withCount, boolean compact) {
        MutableComponent component = new TranslatableComponent("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(new TranslatableComponent(translationKey, ModList.get().size()));
        }
        return component;
    }
    
    private static Screen createModListScreen(Screen lastScreen) {
        if (!BetterModsButton.CONFIG.client().forceDirtBackground) {
            return new ModListScreen(lastScreen);
        }
        if (ModLoaderEnvironment.isModLoaded("catalogue")) {
            // don't want to risk catalogue renaming packages / classes at some point and all of this breaking
            try {
                return CatalogueModListFactory.createCatalogueModListScreen();
            } catch (ClassNotFoundException ignored) {
            }
        }
        return new ModListScreen(lastScreen) {
            @Override
            public void renderBackground(PoseStack pMatrixStack, int pVOffset) {
                this.renderDirtBackground(pVOffset);
            }
        };
    }
}
