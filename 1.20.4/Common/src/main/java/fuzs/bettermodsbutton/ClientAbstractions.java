package fuzs.bettermodsbutton;

import dev.architectury.injectables.annotations.ExpectPlatform;
import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public interface ClientAbstractions {

    @ExpectPlatform
    static ClientConfig getClientConfig() {
        throw new RuntimeException();
    }

    @ExpectPlatform
    static int getModListSize() {
        throw new RuntimeException();
    }

    @ExpectPlatform
    static Screen makeModListScreen(Screen lastScreen) {
        throw new RuntimeException();
    }

    @ExpectPlatform
    static void setModUpdateNotification(Screen screen, @Nullable Button button) {
        throw new RuntimeException();
    }
}
