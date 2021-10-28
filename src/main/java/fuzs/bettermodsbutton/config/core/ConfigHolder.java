package fuzs.bettermodsbutton.config.core;

import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Paths;
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

    static String simpleName(String modId) {
        return String.format("%s.toml", modId);
    }

    static String defaultName(String modId, ModConfig.Type type) {
        return String.format("%s-%s.toml", modId, type.extension());
    }

    static String moveToDir(String configDir, String fileName) {
        return Paths.get(configDir, fileName).toString();
    }

    static Builder create(String modId) {
        return new Builder(modId);
    }

    class Builder {

        private final String modId;

        private Builder(String modId) {
            this.modId = modId;
        }

        /**
         * @param client client config factory
         * @param server server config factory
         * @param <C> client config type
         * @param <S> server config type
         * @return a config holder which only holds both a client config and a server config
         */
        public <C extends AbstractConfig, S extends AbstractConfig> ConfigHolderImpl<C, S> of(Supplier<C> client, Supplier<S> server) {
            return new ConfigHolderImpl<>(this.modId, client, server);
        }

        /**
         * @param client client config factory
         * @param <C> client config type
         * @return a config holder which only holds a client config
         */
        public <C extends AbstractConfig> ConfigHolderImpl<C, AbstractConfig> client(Supplier<C> client) {
            return new ConfigHolderImpl<>(this.modId, client, () -> null);
        }

        /**
         * @param server server config factory
         * @param <S> server config type
         * @return a config holder which only holds a server config
         */
        public <S extends AbstractConfig> ConfigHolderImpl<AbstractConfig, S> server(Supplier<S> server) {
            return new ConfigHolderImpl<>(this.modId, () -> null, server);
        }
    }
}
