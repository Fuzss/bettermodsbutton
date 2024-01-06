package fuzs.bettermodsbutton;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterModsButton {
    public static final String MOD_ID = "bettermodsbutton";
    public static final String MOD_NAME = "Better Mods Button";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
