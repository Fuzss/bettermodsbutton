package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.client.handler.ModScreenHandler;
import fuzs.bettermodsbutton.lib.config.AbstractConfig;
import fuzs.bettermodsbutton.lib.config.annotation.Config;

public class ClientConfig extends AbstractConfig {
    @Config(name = "main_menu_mods_button", description = "Where to place mods button on main menu screen.")
    public ModScreenHandler.MainMenuMode mainMenuMode = ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS;
    @Config(description = "Add mod count to mods button.")
    public boolean modCount = true;
    @Config(name = "pause_screen_mods_button", description = "Where to place mods button on pause menu screen.")
    public ModScreenHandler.PauseScreenMode pauseScreenMode = ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS;
    @Config(description = "Show a small green orb when mod updates are available.")
    public boolean updateNotification = false;
    @Config(description = "Force a dirt background to show for the mod list screen, even when inside of a world where it would normally be transparent.")
    public boolean forceDirtBackground = true;

    public ClientConfig() {
        super("");
    }
}
