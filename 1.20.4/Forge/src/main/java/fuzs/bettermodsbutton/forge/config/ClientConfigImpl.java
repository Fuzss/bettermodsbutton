package fuzs.bettermodsbutton.forge.config;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.config.MainMenuMode;
import fuzs.bettermodsbutton.config.PauseScreenMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Supplier;

public class ClientConfigImpl implements ClientConfig {
    public static final ClientConfigImpl INSTANCE = new ClientConfigImpl();

    private final ForgeConfigSpec spec;
    private final ForgeConfigSpec.EnumValue<MainMenuMode> mainMenuMode;
    private final ForgeConfigSpec.BooleanValue addModCount;
    private final ForgeConfigSpec.EnumValue<PauseScreenMode> pauseScreenMode;
    private final ForgeConfigSpec.BooleanValue updateNotification;
    private final ForgeConfigSpec.IntValue safeArea;

    private ClientConfigImpl() {
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

    @Override
    public Supplier<MainMenuMode> getMainMenuMode() {
        return this.mainMenuMode;
    }

    @Override
    public Supplier<Boolean> getAddModCount() {
        return this.addModCount;
    }

    @Override
    public Supplier<PauseScreenMode> getPauseScreenMode() {
        return this.pauseScreenMode;
    }

    @Override
    public Supplier<Boolean> getUpdateNotification() {
        return this.updateNotification;
    }

    @Override
    public Supplier<Integer> getSafeArea() {
        return this.safeArea;
    }
}
