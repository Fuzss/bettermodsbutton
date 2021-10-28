package fuzs.bettermodsbutton.config.core;

import com.google.common.collect.Maps;
import fuzs.bettermodsbutton.config.core.annotation.AnnotatedConfigBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * abstract config template
 */
public abstract class AbstractConfig {
    /**
     * category name, if no category leave empty
     */
    private final String name;
    /**
     * category comment, only added when this is a category ({@link #name} is present)
     */
    private String[] comment = new String[0];
    private final Map<List<String>, String[]> categoryComments = Maps.newHashMap();

    /**
     * @param name category name
     */
    public AbstractConfig(String name) {
        this.name = name;
    }

    /**
     * setup config from config holder
     * @param builder builder to add entries to
     */
    public final void setupConfig(ForgeConfigSpec.Builder builder, ModConfig.Type type) {
        setupConfig(this, builder, type);
    }

    /**
     * add config entries
     * @param builder builder to add entries to
     */
    protected abstract void addToBuilder(ForgeConfigSpec.Builder builder);

    /**
     * adds entries, category, and category comment
     * @param config config to build
     * @param builder builder to add entries to
     */
    protected static void setupConfig(AbstractConfig config, ForgeConfigSpec.Builder builder, ModConfig.Type type) {
        final boolean withCategory = config.name != null && !config.name.isEmpty();
        if (withCategory) {
            if (config.comment.length != 0) builder.comment(config.comment);
            builder.push(config.name);
        }
//        config.addToBuilder(builder);
        AnnotatedConfigBuilder.serialize(builder, (entry, save) -> ConfigManager.addCallback(type, entry, save), config);
        if (withCategory) {
            builder.pop();
        }
    }

    /**
     * register config entry on the client
     * @param entry source config value object
     * @param action action to perform when value changes (is reloaded)
     * @param <S> config value of a certain type
     * @param <T> type for value
     */
    protected static <S extends ForgeConfigSpec.ConfigValue<T>, T> void registerClient(S entry, Consumer<T> action) {
        ConfigManager.addCallback(ModConfig.Type.CLIENT, entry, action);
    }

    /**
     * register config entry on the server
     * @param entry source config value object
     * @param action action to perform when value changes (is reloaded)
     * @param <S> config value of a certain type
     * @param <T> type for value
     */
    protected static <S extends ForgeConfigSpec.ConfigValue<T>, T> void registerServer(S entry, Consumer<T> action) {
        ConfigManager.addCallback(ModConfig.Type.SERVER, entry, action);
    }
}