package fuzs.bettermodsbutton.config.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Consumer;

public abstract class AbstractConfig {
    private final String name;

    public AbstractConfig(String name) {
        this.name = name;
    }

    public final void setupConfig(ForgeConfigSpec.Builder builder) {
        setupConfig(this, builder);
    }

    protected abstract void addToBuilder(ForgeConfigSpec.Builder builder);

    protected static void setupConfig(AbstractConfig config, ForgeConfigSpec.Builder builder) {
        final boolean withCategory = config.name != null && !config.name.isEmpty();
        if (withCategory) {
            builder.push(config.name);
        }
        config.addToBuilder(builder);
        if (withCategory) {
            builder.pop();
        }
    }

    protected static <S extends ForgeConfigSpec.ConfigValue<T>, T> void registerClient(S entry, Consumer<T> action) {
        ConfigManager.addEntry(ModConfig.Type.CLIENT, entry, action);
    }

    protected static <S extends ForgeConfigSpec.ConfigValue<T>, T> void registerServer(S entry, Consumer<T> action) {
        ConfigManager.addEntry(ModConfig.Type.SERVER, entry, action);
    }
}