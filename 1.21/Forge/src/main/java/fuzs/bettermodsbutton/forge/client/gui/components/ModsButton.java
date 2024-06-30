/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.bettermodsbutton.forge.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Custom button subclass to draw an indicator overlay on the button when updates are available.
 */
@ApiStatus.Internal
public class ModsButton extends Button {
    private static final ResourceLocation VERSION_CHECK_ICONS = ResourceLocation.fromNamespaceAndPath(ForgeVersion.MOD_ID,
            "textures/gui/version_check_icons.png"
    );

    @Nullable
    private VersionChecker.Status showNotification;
    private boolean hasCheckedForUpdates = false;

    public ModsButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (!this.hasCheckedForUpdates) {
            this.showNotification = ClientModLoader.checkForUpdates();
            this.hasCheckedForUpdates = true;
        }

        if (this.showNotification == null || !this.showNotification.shouldDraw() ||
                !FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VERSION_CHECK)) {
            return;
        }

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        guiGraphics.blit(VERSION_CHECK_ICONS,
                x + w - (h / 2 + 4),
                y + (h / 2 - 4),
                this.showNotification.getSheetOffset() * 8,
                (this.showNotification.isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0,
                8,
                8,
                64,
                16
        );
    }
}
