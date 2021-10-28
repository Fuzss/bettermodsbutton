package fuzs.bettermodsbutton.config;

import fuzs.bettermodsbutton.client.handler.ModScreenHandler;
import fuzs.bettermodsbutton.config.core.AbstractConfig;
import fuzs.bettermodsbutton.config.core.ConfigHolder;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends AbstractConfig {
    public ModScreenHandler.MainMenuMode mainMenuMode;
    public boolean modCount;
    public ModScreenHandler.PauseScreenMode pauseScreenMode;
    public boolean updateNotification;

    public ServerConfig() {
        super("");
    }

    @Override
    protected void addToBuilder(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveCallback) {
        saveCallback.accept(builder.comment("Where to place mods button on main menu screen.").defineEnum("Main Menu Mods Button", ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS), v -> this.mainMenuMode = v);
        saveCallback.accept(builder.comment("Add mod count to mods button.").define("Mod Count", true), v -> this.modCount = v);
        saveCallback.accept(builder.comment("Where to place mods button on pause menu screen.").defineEnum("Pause Screen Mods Button", ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS), v -> this.pauseScreenMode = v);
        saveCallback.accept(builder.comment("Show a small green orb when mod updates are available.").define("Update Notification", false), v -> this.updateNotification = v);
    }
}
