package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.lib.config.AbstractConfig;
import fuzs.bettermodsbutton.lib.config.annotation.Config;

public class ClientConfig extends AbstractConfig {
    @Config(name = "main_menu_mods_button", description = "Where to place mods button on main menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.")
    public MainMenuMode mainMenuMode = MainMenuMode.INSERT_BELOW_REALMS;
    @Config(description = "Add mod count to mods button.")
    public boolean modCount = true;
    @Config(name = "pause_screen_mods_button", description = "Where to place mods button on pause menu screen.")
    public PauseScreenMode pauseScreenMode = PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS;
    @Config(description = "Show a small green orb indicating that mod updates are available.")
    public boolean updateNotification = false;
    @Config(description = "Force a dirt background to show for the mod list screen, even when inside of a world where it would normally be transparent.")
    public boolean forceDirtBackground = true;

    public ClientConfig() {
        super("");
    }

    public enum MainMenuMode {
        REPLACE_REALMS, LEFT_TO_REALMS, RIGHT_TO_REALMS, INSERT_BELOW_REALMS, NONE, NO_CHANGE
    }

    public enum PauseScreenMode {
        REPLACE_FEEDBACK, REPLACE_BUGS, REPLACE_FEEDBACK_AND_BUGS, REPLACE_AND_MOVE_LAN, INSERT_AND_MOVE_LAN, INSERT_BELOW_FEEDBACK_AND_BUGS, NONE
    }
}
