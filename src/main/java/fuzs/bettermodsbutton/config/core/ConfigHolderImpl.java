package fuzs.bettermodsbutton.config.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ConfigHolderImpl<C extends AbstractConfig, S extends AbstractConfig> implements ConfigHolder<C, S> {
    @Nullable
    private final C client;
    @Nullable
    private final S server;

    ConfigHolderImpl(@Nonnull Supplier<C> client, @Nonnull Supplier<S> server) {
        this.client = FMLEnvironment.dist.isClient() ? client.get() : null;
        this.server = server.get();
    }

    public void addConfigs(ModLoadingContext context) {
        if (this.client != null) {
            context.registerConfig(ModConfig.Type.CLIENT, this.buildSpec(this.client));
        }
        if (this.server != null) {
            context.registerConfig(ModConfig.Type.SERVER, this.buildSpec(this.server));
        }
    }

    public void addConfigs(ModLoadingContext context, String clientName, String serverName) {
        if (this.client != null) {
            context.registerConfig(ModConfig.Type.CLIENT, this.buildSpec(this.client), clientName);
        }
        if (this.server != null) {
            context.registerConfig(ModConfig.Type.SERVER, this.buildSpec(this.server), serverName);
        }
    }

    private ForgeConfigSpec buildSpec(AbstractConfig config) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        config.setupConfig(builder);
        return builder.build();
    }

    public C client() {
        return this.client;
    }

    public S server() {
        return this.server;
    }
}
