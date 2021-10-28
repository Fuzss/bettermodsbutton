package fuzs.bettermodsbutton;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.config.core.AbstractConfig;
import fuzs.bettermodsbutton.config.core.ConfigHolder;
import fuzs.bettermodsbutton.config.core.ConfigHolderImpl;
import fuzs.bettermodsbutton.config.core.ConfigManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterModsButton.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterModsButton {

    public static final String MODID = "bettermodsbutton";
    public static final String NAME = "Better Mods Button";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, AbstractConfig> CONFIG = ConfigHolder.create(MODID).client(() -> new ClientConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ConfigManager.createManager(MODID, FMLJavaModLoadingContext.get().getModEventBus());
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(ModLoadingContext.get());
    }
}
