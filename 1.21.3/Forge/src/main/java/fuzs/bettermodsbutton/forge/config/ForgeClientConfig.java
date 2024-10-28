package fuzs.bettermodsbutton.forge.config;

import fuzs.bettermodsbutton.config.ClientConfig;
import fuzs.bettermodsbutton.config.MainMenuMode;
import fuzs.bettermodsbutton.config.ModCountMode;
import fuzs.bettermodsbutton.config.PauseScreenMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public class ForgeClientConfig implements ClientConfig {
    public static final ForgeClientConfig INSTANCE = new ForgeClientConfig();

    private final ForgeConfigSpec spec;
    private final ForgeConfigSpec.EnumValue<MainMenuMode> mainMenuMode;
    private final ForgeConfigSpec.EnumValue<ModCountMode> modCountMode;
    private final ForgeConfigSpec.EnumValue<PauseScreenMode> pauseScreenMode;
    private final ForgeConfigSpec.IntValue safeArea;
    private final ForgeConfigSpec.BooleanValue collapseBranding;

    private ForgeClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        this.mainMenuMode = builder.comment(
                        "Where to place mods button on main menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.")
                .defineEnum("main_menu_mods_button", MainMenuMode.INSERT_BELOW_REALMS);
        this.modCountMode = builder.comment("Add mod count to mods button.").defineEnum("mod_count_mode",
                ModCountMode.ADAPTIVE
        );
        this.pauseScreenMode = builder.comment(
                        "Where to place mods button on pause menu screen. Select \"NO_CHANGE\" to prevent any changes to the screen, useful for mod compatibility.")
                .defineEnum("pause_screen_mods_button", PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS);
        this.safeArea = builder.comment(
                "Safe area in pixels from screen border for buttons to not be moved to prevent them from going off-screen.",
                "Not effective for vanilla menu layouts, but can be useful when mods add buttons close to the border."
        ).defineInRange("safe_area", 24, 0, Integer.MAX_VALUE);
        this.collapseBranding = builder.comment(
                "Make title screen game branding more compact to prevent overlapping with menu buttons.").define(
                "collapse_branding", !FMLEnvironment.production);
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
    public Supplier<ModCountMode> getModCountMode() {
        return this.modCountMode;
    }

    @Override
    public Supplier<PauseScreenMode> getPauseScreenMode() {
        return this.pauseScreenMode;
    }

    @Override
    public Supplier<Integer> getSafeArea() {
        return this.safeArea;
    }

    @Override
    public Supplier<Boolean> getCollapseBranding() {
        return this.collapseBranding;
    }
}
