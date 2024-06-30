package fuzs.bettermodsbutton.neoforge.data.client;

import fuzs.bettermodsbutton.client.handler.ModsButtonHandler;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(String modId, PackOutput output) {
        super(output, modId, "en_us");
    }

    @Override
    public void addTranslations() {
        this.add(ModsButtonHandler.KEY_MODS_COUNT, "(%s Loaded)");
        this.add(ModsButtonHandler.KEY_MODS_COUNT_COMPACT, "(%s)");
    }
}
