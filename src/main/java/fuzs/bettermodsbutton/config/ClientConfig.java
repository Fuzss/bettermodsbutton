package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.client.handler.ModScreenHandler;
import fuzs.bettermodsbutton.config.core.AbstractConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig extends AbstractConfig {
    public ModScreenHandler.MainMenuMode mainMenuMode;
    public boolean modCount;
    public ModScreenHandler.PauseScreenMode pauseScreenMode;
    public boolean updateNotification;

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
