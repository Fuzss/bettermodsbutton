package fuzs.bettermodsbutton.neoforge;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.neoforge.config.NeoForgeClientConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(BetterModsButton.MOD_ID)
public class BetterModsButtonNeoForge {

    public BetterModsButtonNeoForge(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.INSTANCE.getSpec());
    }
}
