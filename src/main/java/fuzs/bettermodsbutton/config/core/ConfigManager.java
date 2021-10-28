package fuzs.bettermodsbutton.config.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.bettermodsbutton.BetterModsButton;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {

    private final Map<ModConfig.Type, Collection<Runnable>> typeToSyncCallback = Stream.of(ModConfig.Type.values())
            .collect(Collectors.toMap(Function.identity(), type -> Lists.newArrayList(), (o1, o2) -> o1, () -> Maps.newEnumMap(ModConfig.Type.class)));

    private ConfigManager() {
    }

    public static void createManager(String modId, IEventBus modBus) {
        final ConfigManager manager = new ConfigManager();
        modBus.addListener((final ModConfig.ModConfigEvent evt) -> manager.onModConfig(evt, modId));
        // ModConfigEvent sometimes doesn't fire on start-up, resulting in config values not being synced, so we force it once
        // not sure if this is still an issue though
        modBus.addListener((final FMLLoadCompleteEvent evt) -> manager.sync());
    }

    @SubscribeEvent
    public void onModConfig(final ModConfig.ModConfigEvent evt, String modId) {
        // this is fired on ModEventBus, so mod id check is not necessary here
        // we keep this as it's required on Fabric though due to a dedicated ModEventBus being absent
        if (evt.getConfig().getModId().equals(modId)) {
            final ModConfig.Type type = evt.getConfig().getType();
            this.sync(type);
            if (evt instanceof ModConfig.Reloading) {
                BetterModsButton.LOGGER.info("Reloading {} config for {}", type.extension(), modId);
            }
        }
    }

    private void sync() {
        this.typeToSyncCallback.values().stream()
                .flatMap(Collection::stream)
                .forEach(Runnable::run);
    }

    private void sync(ModConfig.Type type) {
        this.typeToSyncCallback.get(type)
                .forEach(Runnable::run);
    }

    public <S extends ForgeConfigSpec.ConfigValue<T>, T> void addCallback(ModConfig.Type type, S entry, Consumer<T> save) {
        this.typeToSyncCallback.get(type).add(() -> save.accept(entry.get()));
    }

    public static String simpleName(String modId) {
        return String.format("%s.toml", modId);
    }

    public static String defaultName(String modId, ModConfig.Type type) {
        return String.format("%s-%s.toml", modId, type.extension());
    }

    public static String moveToDir(String configDir, String fileName) {
        return Paths.get(configDir, fileName).toString();
    }
}
