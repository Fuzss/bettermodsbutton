package fuzs.bettermodsbutton.neoforge;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.neoforge.config.ClientConfigImpl;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(BetterModsButton.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterModsButtonNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfigImpl.INSTANCE.getSpec());
    }
}
