package fuzs.bettermodsbutton.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();

    private final ForgeConfigSpec spec;
    public final ForgeConfigSpec.EnumValue<MainMenuMode> mainMenuMode;
    public final ForgeConfigSpec.BooleanValue addModCount;
    public final ForgeConfigSpec.EnumValue<PauseScreenMode> pauseScreenMode;
    public final ForgeConfigSpec.BooleanValue updateNotification;
    public final ForgeConfigSpec.IntValue safeArea;

    private ClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        this.mainMenuMode = builder.comment("Where to place mods button on main menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.").defineEnum("main_menu_mods_button", MainMenuMode.INSERT_BELOW_REALMS);
        this.addModCount = builder.comment("Add mod count to mods button.").define("add_mod_count", true);
        this.pauseScreenMode = builder.comment("Where to place mods button on pause menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.").defineEnum("pause_screen_mods_button", PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS);
        this.updateNotification = builder.comment("Show a small green orb indicating that mod updates are available.").define("update_notification", false);
        this.safeArea = builder.comment("Safe area in pixels from screen border for buttons to not be moved to prevent them from going off screen.", "Not effective for vanilla menu layouts, but can be useful when mods add buttons close to the border.").defineInRange("safe_area", 24, 0, Integer.MAX_VALUE);
        this.spec = builder.build();
    }

    public ForgeConfigSpec getSpec() {
        return this.spec;
    }

    public enum MainMenuMode {
        REPLACE_REALMS, LEFT_TO_REALMS, RIGHT_TO_REALMS, INSERT_BELOW_REALMS, NONE, NO_CHANGE

    }
    public enum PauseScreenMode {
        REPLACE_FEEDBACK, REPLACE_BUGS, REPLACE_FEEDBACK_AND_BUGS, REPLACE_AND_MOVE_LAN, INSERT_AND_MOVE_LAN, INSERT_BELOW_FEEDBACK_AND_BUGS, NONE, NO_CHANGE

    }
}
