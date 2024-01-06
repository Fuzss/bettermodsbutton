package fuzs.bettermodsbutton.forge;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.forge.client.handler.ModUpdateNotificationHandler;
import fuzs.bettermodsbutton.forge.config.ClientConfigImpl;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class ClientAbstractionsImpl {

    public static ClientConfig getClientConfig() {
        return ClientConfigImpl.INSTANCE;
    }

    public static int getModListSize() {
        return ModList.get().size();
    }

    public static Screen makeModListScreen(Screen lastScreen) {
        return new ModListScreen(lastScreen);
    }

    public static void setModUpdateNotification(Screen screen, @Nullable Button button) {
        ModUpdateNotificationHandler.setModUpdateNotification(screen, button);
    }
}
