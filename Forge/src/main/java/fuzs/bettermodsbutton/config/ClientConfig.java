package fuzs.bettermodsbutton.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();

    private final ForgeConfigSpec spec;
    public final ForgeConfigSpec.EnumValue<MainMenuMode> mainMenuMode;
    public final ForgeConfigSpec.BooleanValue modCount;
    public final ForgeConfigSpec.EnumValue<PauseScreenMode> pauseScreenMode;
    public final ForgeConfigSpec.BooleanValue updateNotification;
    public final ForgeConfigSpec.BooleanValue forceDirtBackground;

    private ClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        this.mainMenuMode = builder.comment("Where to place mods button on main menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.").defineEnum("main_menu_mods_button", MainMenuMode.INSERT_BELOW_REALMS);
        this.modCount = builder.comment("Add mod count to mods button.").define("mod_count", true);
        this.pauseScreenMode = builder.comment("Where to place mods button on pause menu screen.").defineEnum("pause_screen_mods_button", PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS);
        this.updateNotification = builder.comment("Show a small green orb indicating that mod updates are available.").define("update_notification", false);
        this.forceDirtBackground = builder.comment("Force a dirt background to show for the mod list screen, even when inside of a world where it would normally be transparent.").define("force_dirt_background", true);
        this.spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }
    public enum MainMenuMode {
        REPLACE_REALMS, LEFT_TO_REALMS, RIGHT_TO_REALMS, INSERT_BELOW_REALMS, NONE, NO_CHANGE

    }
    public enum PauseScreenMode {
        REPLACE_FEEDBACK, REPLACE_BUGS, REPLACE_FEEDBACK_AND_BUGS, REPLACE_AND_MOVE_LAN, INSERT_AND_MOVE_LAN, INSERT_BELOW_FEEDBACK_AND_BUGS, NONE

    }
}
