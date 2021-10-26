package fuzs.bettermodsbutton.config.core;

import java.util.function.Supplier;

public interface ConfigHolder<C extends AbstractConfig, S extends AbstractConfig> {

    C client();

    S server();

    static <C extends AbstractConfig, S extends AbstractConfig> ConfigHolderImpl<C, S> of(Supplier<C> client, Supplier<S> server) {
        return new ConfigHolderImpl<>(client, server);
    }

    static <C extends AbstractConfig> ConfigHolderImpl<C, AbstractConfig> ofClient(Supplier<C> client) {
        return new ConfigHolderImpl<>(client, () -> null);
    }

    static <S extends AbstractConfig> ConfigHolderImpl<AbstractConfig, S> ofServer(Supplier<S> server) {
        return new ConfigHolderImpl<>(() -> null, server);
    }
}
