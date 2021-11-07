package fuzs.bettermodsbutton.compat.catalogue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.gui.screens.Screen;

public class CatalogueModListFactory {

    public static Screen createCatalogueModListScreen() throws ClassNotFoundException {
        return new CatalogueModListScreen() {

            @Override
            public void renderBackground(PoseStack pMatrixStack, int pVOffset) {
                this.renderDirtBackground(pVOffset);
            }
        };
    }
}
