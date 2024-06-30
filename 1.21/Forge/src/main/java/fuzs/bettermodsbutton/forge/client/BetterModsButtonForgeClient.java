package fuzs.bettermodsbutton.forge.client;

import fuzs.bettermodsbutton.BetterModsButton;
import fuzs.bettermodsbutton.client.handler.ModsButtonHandler;
import fuzs.bettermodsbutton.service.ClientAbstractions;
import net.minecraft.DetectedVersion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import net.minecraftforge.common.ForgeI18n;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.internal.BrandingControl;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = BetterModsButton.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BetterModsButtonForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        registerEventHandlers(MinecraftForge.EVENT_BUS);
    }

    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final ScreenEvent.Init.Post evt) -> {
            ModsButtonHandler.onAfterInitScreen(evt.getScreen().getMinecraft(),
                    evt.getScreen(),
                    evt.getListenersList(),
                    evt::addListener,
                    evt::removeListener
            );
            disableModUpdateNotification(evt.getScreen());
        });
        eventBus.addListener((final ScreenEvent.Opening evt) -> {
            setCollapsedBrandingControl(evt.getScreen());
        });
    }

    private static void disableModUpdateNotification(Screen screen) {
        if (screen.getClass() == TitleScreen.class) {
            try {
                Field modUpdateNotificationField = TitleScreen.class.getDeclaredField("modUpdateNotification");
                modUpdateNotificationField.setAccessible(true);
                MethodHandles.lookup()
                        .unreflectSetter(modUpdateNotificationField)
                        .invoke(screen, new TitleScreenModUpdateIndicator(null));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    private static void setCollapsedBrandingControl(Screen screen) {
        if (!ClientAbstractions.INSTANCE.getClientConfig().getCollapseBranding().get()) return;
        if (screen.getClass() == TitleScreen.class) {
            try {
                Field field = BrandingControl.class.getDeclaredField("brandings");
                field.setAccessible(true);
                String loadingMods = ForgeI18n.parseMessage("fml.menu.loadingmods",
                        ClientAbstractions.INSTANCE.getModListSize()
                );
                String s = "Minecraft " + DetectedVersion.BUILT_IN.getName() + "/Forge" + " (" + loadingMods + ")";
                MethodHandles.lookup().unreflectSetter(field).invoke(Collections.singletonList(s));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
