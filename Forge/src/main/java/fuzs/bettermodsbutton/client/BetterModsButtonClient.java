package fuzs.bettermodsbutton.client;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.client.handler.ModsButtonHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = BetterModsButton.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BetterModsButtonClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener(ModsButtonHandler::onScreen$Init$Post);
        MinecraftForge.EVENT_BUS.addListener(ModsButtonHandler::onScreen$Render);
        MinecraftForge.EVENT_BUS.addListener(ModsButtonHandler::onScreen$Closing);
    }
}
