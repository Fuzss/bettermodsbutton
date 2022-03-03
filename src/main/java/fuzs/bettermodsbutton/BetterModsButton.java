package fuzs.bettermodsbutton;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.lib.config.AbstractConfig;
import fuzs.bettermodsbutton.lib.config.ConfigHolder;
import fuzs.bettermodsbutton.lib.config.ConfigHolderImpl;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BetterModsButton.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BetterModsButton {
    public static final String MOD_ID = "bettermodsbutton";
    public static final String MOD_NAME = "Better Mods Button";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<ClientConfig, AbstractConfig> CONFIG = ConfigHolder.client(() -> new ClientConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (remote, server) -> true));
    }
}
