package fuzs.bettermodsbutton.neoforge;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.neoforge.config.NeoForgeClientConfig;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(BetterModsButton.MOD_ID)
public class BetterModsButtonNeoForge {

    public BetterModsButtonNeoForge(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.INSTANCE.getSpec());
        registerLoadingHandlers(modContainer.getEventBus(), modContainer.getModInfo().getDescription());
    }

    private static void registerLoadingHandlers(IEventBus eventBus, String modDescription) {
        eventBus.addListener((final GatherDataEvent.Client event) -> {
            event.getGenerator()
                    .addProvider(true,
                            PackMetadataGenerator.forFeaturePack(event.getGenerator().getPackOutput(),
                                    Component.literal(modDescription)));
        });
    }

    public static boolean isDevelopmentEnvironment() {
        return isDevelopmentEnvironment(BetterModsButton.MOD_ID);
    }

    public static boolean isDevelopmentEnvironment(String modId) {
        if (FMLEnvironment.production) {
            return false;
        } else {
            return Boolean.getBoolean(modId + ".isDevelopmentEnvironment");
        }
    }
}
