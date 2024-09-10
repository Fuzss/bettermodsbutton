package fuzs.bettermodsbutton.forge;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.forge.config.ForgeClientConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(BetterModsButton.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterModsButtonForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.INSTANCE.getSpec());
    }
}
