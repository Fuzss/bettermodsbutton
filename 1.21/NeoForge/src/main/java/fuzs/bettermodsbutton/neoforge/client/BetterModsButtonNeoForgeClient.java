package fuzs.bettermodsbutton.neoforge.client;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.client.handler.ModsButtonHandler;
import fuzs.bettermodsbutton.neoforge.data.client.ModLanguageProvider;
import fuzs.bettermodsbutton.service.ClientAbstractions;
import net.minecraft.DetectedVersion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.BrandingControl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;

@Mod(value = BetterModsButton.MOD_ID, dist = Dist.CLIENT)
public class BetterModsButtonNeoForgeClient {

    public BetterModsButtonNeoForgeClient(ModContainer modContainer) {
        registerLoadingHandlers(modContainer.getEventBus());
        registerEventHandlers(NeoForge.EVENT_BUS);
    }

    private static void registerLoadingHandlers(IEventBus eventBus) {
        eventBus.addListener((final GatherDataEvent evt) -> {
            evt.getGenerator()
                    .addProvider(evt.includeClient(),
                            new ModLanguageProvider(BetterModsButton.MOD_ID, evt.getGenerator().getPackOutput())
                    );
        });
    }

    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final ScreenEvent.Init.Post evt) -> {
            ModsButtonHandler.onAfterInitScreen(evt.getScreen().getMinecraft(),
                    evt.getScreen(),
                    evt.getListenersList(),
                    evt::addListener,
                    evt::removeListener
            );
        });
        eventBus.addListener((final ScreenEvent.Opening evt) -> {
            setCollapsedBrandingControl(evt.getScreen());
        });
    }

    private static void setCollapsedBrandingControl(Screen screen) {
        if (!ClientAbstractions.INSTANCE.getClientConfig().getCollapseBranding().get()) return;
        if (screen.getClass() == TitleScreen.class) {
            try {
                Field field = BrandingControl.class.getDeclaredField("brandings");
                field.setAccessible(true);
                String loadingMods = FMLTranslations.parseMessageWithFallback("fml.menu.loadingmods",
                        () -> "%s Mods",
                        ClientAbstractions.INSTANCE.getModListSize()
                );
                String s = "Minecraft " + DetectedVersion.BUILT_IN.getName() + "/" + ForgeSnapshotsMod.BRANDING_NAME +
                        " (" + loadingMods + ")";
                MethodHandles.lookup().unreflectSetter(field).invoke(Collections.singletonList(s));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
