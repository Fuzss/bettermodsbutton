package fuzs.bettermodsbutton.config;

import com.google.common.collect.Lists;
import fuzs.bettermodsbutton.client.handler.ModScreenHandler;
import fuzs.bettermodsbutton.config.core.AbstractConfig;
import fuzs.bettermodsbutton.config.core.ConfigHolder;
import fuzs.bettermodsbutton.config.core.annotation.Config;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ClientConfig extends AbstractConfig {
    @Config(name = "Main Menu Mods Button", description = "Where to place mods button on main menu screen.", category = {"path", "to", "something"})
    @Config.AllowedValues(values = "INSERT_BELOW_REALMS")
    public ModScreenHandler.MainMenuMode mainMenuMode = ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS;
    @Config(name = "Mod Count", description = "Add mod count to mods button.")
    public boolean modCount = true;
    @Config(name = "Pause Screen Mods Button", description = "Where to place mods button on pause menu screen.", category = {"path", "to", "something"})
    public ModScreenHandler.PauseScreenMode pauseScreenMode = ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS;
    @Config(name = "Update Notification", description = "Show a small green orb when mod updates are available.")
    public boolean updateNotification = false;
    @Config(description = "Is this even allowed???", category = {"path", "to", "something", "deeper"})
    @Config.FloatRange(min = 0.0F)
    public float justACheesyFloat = 1.0F;
    @Config(description = "Is this even allowed2222???", category = {"path", "to", "something"})
    @Config.FloatRange(min = 0.5F)
    public float justACheesyFloat2 = 1.0F;
    @Config(description = "no comment")
    @Config.AllowedValues(values = "INSERT_BELOW_REALMS")
    public static List<ModScreenHandler.MainMenuMode> mappyWithGold = Lists.newArrayList(ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS);

    public ClientConfig() {
        super("");
        this.addComment(Lists.newArrayList("path", "to", "something", "deeper"), "Hi this is a test", "How many lines can there be");
        this.addComment(Lists.newArrayList("path", "to"), "Hi this is a test", "How many lines cfsdfsdgfsafgan there be");
        this.addComment(Lists.newArrayList("path"), "Hi this is a test", "Htewrtfgwergwergewrgow many lines can there be");
    }

    @Override
    protected void addToBuilder(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback addCallback) {
        addCallback.accept(builder.comment("Where to place mods button on main menu screen.").defineEnum("Main Menu Mods Button2", ModScreenHandler.MainMenuMode.INSERT_BELOW_REALMS), v -> this.mainMenuMode = v);
        addCallback.accept(builder.comment("Add mod count to mods button.").define("Mod Count2", true), v -> this.modCount = v);
        addCallback.accept(builder.comment("Where to place mods button on pause menu screen.").defineEnum("Pause Screen Mods Button2", ModScreenHandler.PauseScreenMode.INSERT_BELOW_FEEDBACK_AND_BUGS), v -> this.pauseScreenMode = v);
        addCallback.accept(builder.comment("Show a small green orb when mod updates are available.").define("Update Notification2", false), v -> this.updateNotification = v);
    }
}
