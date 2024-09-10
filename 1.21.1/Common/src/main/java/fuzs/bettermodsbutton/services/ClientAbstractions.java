package fuzs.bettermodsbutton.services;

import fuzs.bettermodsbutton.config.ClientConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClientAbstractions {
    ClientAbstractions INSTANCE = ServiceProviderLoader.load(ClientAbstractions.class);

    ClientConfig getClientConfig();

    int getModListSize();

    String getModListMessage();

    @NotNull
    Button getNewModsButton(Screen screen, @Nullable Button oldButton);
}
