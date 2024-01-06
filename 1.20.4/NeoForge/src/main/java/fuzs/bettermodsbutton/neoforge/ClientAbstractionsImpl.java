package fuzs.bettermodsbutton.neoforge;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.neoforge.client.handler.ModUpdateNotificationHandler;
import fuzs.bettermodsbutton.neoforge.config.ClientConfigImpl;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ModListScreen;
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
