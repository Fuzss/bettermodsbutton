package fuzs.bettermodsbutton.compat.catalogue;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.gui.screen.Screen;

public class CatalogueModListFactory {

    public static Screen createCatalogueModListScreen() throws ClassNotFoundException {
        return new CatalogueModListScreen() {

            @Override
            public void renderBackground(MatrixStack pMatrixStack, int pVOffset) {
                this.renderDirtBackground(pVOffset);
            }
        };
    }
}
