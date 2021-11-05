package fuzs.bettermodsbutton.client.handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.compat.catalogue.CatalogueModListFactory;
import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.lib.core.ModLoaderEnvironment;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.gui.NotificationModUpdateScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("CodeBlock2Expr")
public class ModScreenHandler {
    private NotificationModUpdateScreen gameMenuNotification;

    @SubscribeEvent
    public void onInitGui(final GuiScreenEvent.InitGuiEvent.Post evt) {
        if (evt.getGui().getClass() == MainMenuScreen.class) {
            this.handleMainMenu(evt);
        } else if (evt.getGui() instanceof IngameMenuScreen) {
            this.handlePauseScreen(evt);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(final GuiScreenEvent.DrawScreenEvent evt) {
        if (evt.getGui() instanceof IngameMenuScreen) {
            this.gameMenuNotification.render(evt.getMatrixStack(), evt.getMouseX(), evt.getMouseY(), evt.getRenderPartialTicks());
        }
    }

    private void handleMainMenu(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (BetterModsButton.CONFIG.client().mainMenuMode == ClientConfig.MainMenuMode.NO_CHANGE) return;
        final boolean modCount = BetterModsButton.CONFIG.client().modCount;
        this.getButton(evt.getWidgetList(), "fml.menu.mods").ifPresent(evt::removeWidget);
        Button modsButton = null;
        switch (BetterModsButton.CONFIG.client().mainMenuMode) {
            case INSERT_BELOW_REALMS:
                for (Widget widget : evt.getWidgetList()) {
                    if (evt.getGui().height / 4 + 48 + 72 + 12 <= widget.y) {
                        widget.y += 12;
                    } else {
                        widget.y -= 12;
                    }
                }
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> {
                    widget.setWidth(200);
                    widget.x = evt.getGui().width / 2 - 100;
                });
                modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 3 - 12, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case NONE:
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> {
                    widget.setWidth(200);
                    widget.x = evt.getGui().width / 2 - 100;
                });
                break;
            case LEFT_TO_REALMS:
                modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case RIGHT_TO_REALMS:
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(widget -> widget.x = evt.getGui().width / 2 - 100);
                modsButton = new Button(evt.getGui().width / 2 + 2, evt.getGui().height / 4 + 48 + 24 * 2, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case REPLACE_REALMS:
                this.getButton(evt.getWidgetList(), "menu.online").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 - 100, evt.getGui().height / 4 + 48 + 24 * 2, 200, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
        }
        if (modsButton != null) evt.addWidget(modsButton);
        ObfuscationReflectionHelper.setPrivateValue(MainMenuScreen.class, (MainMenuScreen) evt.getGui(), NotificationModUpdateScreen.init((MainMenuScreen) evt.getGui(), BetterModsButton.CONFIG.client().updateNotification ? modsButton : null), "modUpdateNotification");
    }

    private void handlePauseScreen(GuiScreenEvent.InitGuiEvent.Post evt) {
        if (BetterModsButton.CONFIG.client().pauseScreenMode == ClientConfig.PauseScreenMode.NONE) return;
        final boolean modCount = BetterModsButton.CONFIG.client().modCount;
        Button modsButton = null;
        switch (BetterModsButton.CONFIG.client().pauseScreenMode) {
            case INSERT_BELOW_FEEDBACK_AND_BUGS:
                for (Widget widget : evt.getWidgetList()) {
                    if (evt.getGui().height / 4 + 96 - 16 <= widget.y) {
                        widget.y += 12;
                    } else {
                        widget.y -= 12;
                    }
                }
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 96 - 16 - 12, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case REPLACE_FEEDBACK:
                this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case REPLACE_BUGS:
                this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 + 4, evt.getGui().height / 4 + 72 - 16, 98, 20, this.getModsComponent(modCount, true), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case REPLACE_FEEDBACK_AND_BUGS:
                this.getButton(evt.getWidgetList(), "menu.sendFeedback").ifPresent(evt::removeWidget);
                this.getButton(evt.getWidgetList(), "menu.reportBugs").ifPresent(evt::removeWidget);
                modsButton = new Button(evt.getGui().width / 2 - 102, evt.getGui().height / 4 + 72 - 16, 204, 20, this.getModsComponent(modCount, false), button -> {
                    evt.getGui().getMinecraft().setScreen(createModListScreen(evt.getGui()));
                });
                break;
            case REPLACE_AND_MOVE_LAN:
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
                break;
            case INSERT_AND_MOVE_LAN:
                for (Widget widget : evt.getWidgetList()) {
                    if (evt.getGui().height / 4 + 96 - 16 <= widget.y) {
                        widget.y += 12;
                    } else {
                        widget.y -= 12;
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
                break;
        }
        if (modsButton != null) evt.addWidget(modsButton);
        this.gameMenuNotification = new NotificationModUpdateScreen(BetterModsButton.CONFIG.client().updateNotification ? modsButton : null);
        this.gameMenuNotification.resize(evt.getGui().getMinecraft(), evt.getGui().width, evt.getGui().height);
        this.gameMenuNotification.init();
    }

    private Optional<Widget> getButton(List<Widget> widgets, String s) {
        for (Widget widget : widgets) {
            if (this.containsKey(widget, s)) {
                return Optional.of(widget);
            }
        }
        return Optional.empty();
    }

    private boolean containsKey(Widget button, String key) {
        final ITextComponent message = button.getMessage();
        return message instanceof TranslationTextComponent && ((TranslationTextComponent) message).getKey().equals(key);
    }

    private ITextComponent getModsComponent(boolean withCount, boolean compact) {
        IFormattableTextComponent component = new TranslationTextComponent("fml.menu.mods");
        if (withCount) {
            String translationKey = compact ? "button.mods.count.compact" : "button.mods.count";
            component = component.append(" ").append(new TranslationTextComponent(translationKey, ModList.get().size()));
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
            public void renderBackground(MatrixStack pMatrixStack, int pVOffset) {
                this.renderDirtBackground(pVOffset);
            }
        };
    }
}
