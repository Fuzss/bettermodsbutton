package fuzs.bettermodsbutton;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BetterModsButton.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterModsButton {
    public static final String MOD_ID = "bettermodsbutton";
    public static final String MOD_NAME = "Better Mods Button";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.INSTANCE.getSpec());
    }
}
