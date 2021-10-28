package fuzs.bettermodsbutton.config.core;

import com.google.common.collect.Lists;
import fuzs.bettermodsbutton.BetterModsButton;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * implementation of {@link ConfigHolder} for building configs and handling config creation depending on physical side
 * @param <C> client config type
 * @param <S> server config type
 */
public class ConfigHolderImpl<C extends AbstractConfig, S extends AbstractConfig> implements ConfigHolder<C, S> {
    /**
     * client config
     */
    @Nullable
    private final C client;
    /**
     * server config
     */
    @Nullable
    private final S server;
    private final List<Runnable> clientCallbacks = Lists.newArrayList();
    private final List<Runnable> serverCallbacks = Lists.newArrayList();

    /**
     * client config will only be created on physical client
     * @param client client config factory
     * @param server server config factory
     */
    ConfigHolderImpl(String modId, @Nonnull Supplier<C> client, @Nonnull Supplier<S> server) {
        this.client = FMLEnvironment.dist.isClient() ? client.get() : null;
        this.server = server.get();
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((final ModConfig.ModConfigEvent evt) -> this.onModConfig(evt, modId));
        // ModConfigEvent sometimes doesn't fire on start-up, resulting in config values not being synced, so we force it once
        // not sure if this is still an issue though
        modBus.addListener((final FMLLoadCompleteEvent evt) -> {
            this.clientCallbacks.forEach(Runnable::run);
            this.serverCallbacks.forEach(Runnable::run);
        });
    }

    @SubscribeEvent
    public void onModConfig(final ModConfig.ModConfigEvent evt, String modId) {
        // this is fired on ModEventBus, so mod id check is not necessary here
        // we keep this as it's required on Fabric though due to a dedicated ModEventBus being absent
        if (evt.getConfig().getModId().equals(modId)) {
            final ModConfig.Type type = evt.getConfig().getType();
            switch (type) {
                case CLIENT:
                    this.clientCallbacks.forEach(Runnable::run);
                    break;
                case SERVER:
                    this.serverCallbacks.forEach(Runnable::run);
                    break;
            }
            if (evt instanceof ModConfig.Reloading) {
                BetterModsButton.LOGGER.info("Reloading {} config for {}", type.extension(), modId);
            }
        }
    }

    /**
     * register configs if present
     * @param context mod context to register to
     */
    public void addConfigs(ModLoadingContext context) {
        if (this.client != null) {
            context.registerConfig(ModConfig.Type.CLIENT, this.buildSpec(this.client, ModConfig.Type.CLIENT));
        }
        if (this.server != null) {
            context.registerConfig(ModConfig.Type.SERVER, this.buildSpec(this.server, ModConfig.Type.SERVER));
        }
    }

    /** register configs, allows for custom file names
     * @param context mod context to register to
     * @param clientName client config file name
     * @param serverName server config file name
     */
    public void addConfigs(ModLoadingContext context, String clientName, String serverName) {
        if (this.client != null) {
            context.registerConfig(ModConfig.Type.CLIENT, this.buildSpec(this.client, ModConfig.Type.CLIENT), clientName);
        }
        if (this.server != null) {
            context.registerConfig(ModConfig.Type.SERVER, this.buildSpec(this.server, ModConfig.Type.SERVER), serverName);
        }
    }

    /**
     * creates a builder and buildes the config from it
     * @param config config to build
     * @return built spec
     */
    private ForgeConfigSpec buildSpec(AbstractConfig config, ModConfig.Type type) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        config.setupConfig(builder, type);
        return builder.build();
    }

    @Override
    public C client() {
        return this.client;
    }

    @Override
    public S server() {
        return this.server;
    }
}
