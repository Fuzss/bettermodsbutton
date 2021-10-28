package fuzs.bettermodsbutton.config.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * a config holder holds two separate configs for both logical server and logical client
 * one or both types may not be present, depending on mod requirements and physical side
 * @param <C> client config type
 * @param <S> server config type
 */
public interface ConfigHolder<C extends AbstractConfig, S extends AbstractConfig> {

    /**
     * @return client config from this holder, possibly null
     */
    C client();

    /**
     * @return server config from this holder, possibly null
     */
    S server();

    void addClientCallback(Runnable callback);

    void addServerCallback(Runnable callback);

    static String simpleName(String modId) {
        return String.format("%s.toml", modId);
    }

    static String defaultName(String modId, ModConfig.Type type) {
        return String.format("%s-%s.toml", modId, type.extension());
    }

    static String moveToDir(String configDir, String fileName) {
        return Paths.get(configDir, fileName).toString();
    }

    /**
     * @param client client config factory
     * @param server server config factory
     * @param <C> client config type
     * @param <S> server config type
     * @return a config holder which only holds both a client config and a server config
     */
    static <C extends AbstractConfig, S extends AbstractConfig> ConfigHolderImpl<C, S> of(Supplier<C> client, Supplier<S> server) {
        return new ConfigHolderImpl<>(client, server);
    }

    /**
     * @param client client config factory
     * @param <C> client config type
     * @return a config holder which only holds a client config
     */
    static <C extends AbstractConfig> ConfigHolderImpl<C, AbstractConfig> client(Supplier<C> client) {
        return new ConfigHolderImpl<>(client, () -> null);
    }

    /**
     * @param server server config factory
     * @param <S> server config type
     * @return a config holder which only holds a server config
     */
    static <S extends AbstractConfig> ConfigHolderImpl<AbstractConfig, S> server(Supplier<S> server) {
        return new ConfigHolderImpl<>(() -> null, server);
    }

    @FunctionalInterface
    interface ConfigCallback {

        <T> void accept(ForgeConfigSpec.ConfigValue<T> entry, Consumer<T> save);
    }
}
