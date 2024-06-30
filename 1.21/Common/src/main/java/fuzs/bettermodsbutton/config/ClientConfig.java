package fuzs.bettermodsbutton.config;

import java.util.function.Supplier;

public interface ClientConfig {

    Supplier<MainMenuMode> getMainMenuMode();

    Supplier<Boolean> getAddModCount();

    Supplier<PauseScreenMode> getPauseScreenMode();

    Supplier<Integer> getSafeArea();

    Supplier<Boolean> getCollapseBranding();
}
