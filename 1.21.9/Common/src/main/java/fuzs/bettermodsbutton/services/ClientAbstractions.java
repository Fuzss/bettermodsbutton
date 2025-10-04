package fuzs.bettermodsbutton.services;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClientAbstractions {
    ClientAbstractions INSTANCE = ServiceProviderLoader.load(ClientAbstractions.class);

    boolean isDevelopmentEnvironment();

    default boolean isDevelopmentEnvironment(String modId) {
        if (!this.isDevelopmentEnvironment()) {
            return false;
        } else {
            return Boolean.getBoolean(modId + ".isDevelopmentEnvironment");
        }
    }

    ClientConfig getClientConfig();

    int getModListSize();

    String getModListMessage();

    @NotNull Button getNewModsButton(Screen screen, @Nullable Button oldButton);
}
