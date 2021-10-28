package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.client.handler.ModScreenHandler;
import fuzs.bettermodsbutton.config.core.AbstractConfig;
import fuzs.bettermodsbutton.config.core.annotation.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig extends AbstractConfig {
    @Config(name = "Main Menu Mods Button", description = "Where to place mods button on main menu screen.")
    @Config.AllowedValues(values = "INSERT_BELOW_REALMS")
    public ModScreenHandler.MainMenuMode mainMenuMode = ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS;
    @Config(name = "Mod Count", description = "Add mod count to mods button.")
    public boolean modCount = true;
    @Config(name = "Pause Screen Mods Button", description = "Where to place mods button on pause menu screen.")
    public ModScreenHandler.PauseScreenMode pauseScreenMode = ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS;
    @Config(name = "Update Notification", description = "Show a small green orb when mod updates are available.")
    public boolean updateNotification = false;
    @Config(description = "Is this even allowed???", category = {"path", "to", "something", "deeper"})
    @Config.FloatRange(min = 0.0F)
    public float justACheesyFloat = 1.0F;
    @Config(description = "Is this even allowed2222???", category = {"path", "to", "something"})
    @Config.FloatRange(min = 0.5F)
    public float justACheesyFloat2 = 1.0F;

    public ClientConfig() {
        super("");
    }

    @Override
    protected void addToBuilder(ForgeConfigSpec.Builder builder) {
        registerClient(builder.comment("Where to place mods button on main menu screen.").defineEnum("Main Menu Mods Button", ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS), v -> this.mainMenuMode = v);
        registerClient(builder.comment("Add mod count to mods button.").define("Mod Count", true), v -> this.modCount = v);
        registerClient(builder.comment("Where to place mods button on pause menu screen.").defineEnum("Pause Screen Mods Button", ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS), v -> this.pauseScreenMode = v);
        registerClient(builder.comment("Show a small green orb when mod updates are available.").define("Update Notification", false), v -> this.updateNotification = v);
    }
}
