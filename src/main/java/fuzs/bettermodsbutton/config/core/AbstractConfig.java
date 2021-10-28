package fuzs.bettermodsbutton.config.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.bettermodsbutton.config.core.annotation.ConfigBuilder;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Map;

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
    public final void setupConfig(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback addCallback) {
        setupConfig(this, builder, addCallback);
    }

    /**
     * add config entries
     * @param builder builder to add entries to
     */
    protected void addToBuilder(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback addCallback) {
    }

    protected void addComment(String... comment) {
        this.addComment(Lists.newArrayList(), comment);
    }

    protected void addComment(List<String> path, String... comment) {
        this.categoryComments.put(path, comment);
    }

    /**
     * adds entries, category, and category comment
     * @param config config to build
     * @param builder builder to add entries to
     */
    protected static void setupConfig(AbstractConfig config, ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback addCallback) {
        final boolean withCategory = config.name != null && !config.name.isEmpty();
        if (withCategory) {
            final String[] comment = config.categoryComments.get(Lists.<String>newArrayList());
            if (comment != null) builder.comment(comment);
            builder.push(config.name);
        }
        ConfigBuilder.serialize(builder, addCallback, Maps.newHashMap(config.categoryComments), config);
        config.addToBuilder(builder, addCallback);
        if (withCategory) {
            builder.pop();
        }
    }
}