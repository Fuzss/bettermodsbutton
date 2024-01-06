package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.config.MainMenuMode;
import fuzs.bettermodsbutton.config.PauseScreenMode;

import java.util.function.Supplier;

public interface ClientConfig {
    Supplier<MainMenuMode> getMainMenuMode();

    Supplier<Boolean> getAddModCount();

    Supplier<PauseScreenMode> getPauseScreenMode();

    Supplier<Boolean> getUpdateNotification();

    Supplier<Integer> getSafeArea();
}
