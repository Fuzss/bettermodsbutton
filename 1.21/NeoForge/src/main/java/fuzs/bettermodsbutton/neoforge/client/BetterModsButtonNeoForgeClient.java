package fuzs.bettermodsbutton.neoforge.client;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.client.handler.ModsButtonHandler;
import fuzs.bettermodsbutton.services.ClientAbstractions;
import net.minecraft.DetectedVersion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforge.internal.BrandingControl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;

@Mod(value = BetterModsButton.MOD_ID, dist = Dist.CLIENT)
public class BetterModsButtonNeoForgeClient {

    public BetterModsButtonNeoForgeClient(ModContainer modContainer) {
        registerEventHandlers(NeoForge.EVENT_BUS);
    }

    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final ScreenEvent.Init.Post evt) -> {
            ModsButtonHandler.onAfterInitScreen(evt.getScreen().getMinecraft(),
                    evt.getScreen(),
                    evt.getListenersList(),
                    evt::addListener,
                    evt::removeListener
            );
            setCollapsedBrandingControl(evt.getScreen());
        });
    }

    private static void setCollapsedBrandingControl(Screen screen) {
        if (!ClientAbstractions.INSTANCE.getClientConfig().getCollapseBranding().get()) return;
        if (screen.getClass() == TitleScreen.class) {
            try {
                Field brandings = BrandingControl.class.getDeclaredField("brandings");
                brandings.setAccessible(true);
                String s = "Minecraft " + DetectedVersion.BUILT_IN.getName() + "/" + ForgeSnapshotsMod.BRANDING_NAME +
                        ClientAbstractions.INSTANCE.getModListMessage();
                MethodHandles.lookup().unreflectSetter(brandings).invoke(Collections.singletonList(s));
                Field overCopyrightBrandings = BrandingControl.class.getDeclaredField("overCopyrightBrandings");
                overCopyrightBrandings.setAccessible(true);
                MethodHandles.lookup().unreflectSetter(overCopyrightBrandings).invoke(Collections.emptyList());
            } catch (Throwable throwable) {
                BetterModsButton.LOGGER.error("Unable to set collapsed branding control", throwable);
            }
        }
    }
}
