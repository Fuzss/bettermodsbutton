package fuzs.bettermodsbutton.config.core.annotation;

import com.google.common.base.CaseFormat;
import com.google.common.collect.*;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AnnotatedConfigBuilder {

    public static <T> void serialize(ForgeConfigSpec.Builder builder, BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, Object target) {
        serialize(builder, target.getClass(), saveConsumer, Maps.newHashMap(), target);
    }

    public static <T> void serialize(ForgeConfigSpec.Builder builder, BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, Class<?> target) {
        serialize(builder, target, saveConsumer, Maps.newHashMap(), null);
    }

    public static <T> void serialize(ForgeConfigSpec.Builder builder, BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, Map<List<String>, String[]> categoryComments, Object target) {
        serialize(builder, target.getClass(), saveConsumer, categoryComments, target);
    }

    public static <T> void serialize(ForgeConfigSpec.Builder builder, BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, Map<List<String>, String[]> categoryComments, Class<?> target) {
        serialize(builder, target, saveConsumer, categoryComments, null);
    }

    public static <T, S> void serialize(final ForgeConfigSpec.Builder builder, Class<? extends T> target, final BiConsumer<ForgeConfigSpec.ConfigValue<S>, Consumer<S>> saveConsumer, Map<List<String>, String[]> categoryComments, @Nullable T instance) {
        Multimap<List<String>, Field> pathToField = HashMultimap.create();
        for (Field field : target.getDeclaredFields()) {
            Config annotation = field.getDeclaredAnnotation(Config.class);
            if (annotation != null) {
                pathToField.put(Lists.newArrayList(annotation.category()), field);
            }
        }
        for (Map.Entry<List<String>, Collection<Field>> entry : pathToField.asMap().entrySet()) {
            final List<String> path = entry.getKey();
            Optional.ofNullable(categoryComments.get(path)).ifPresent(builder::comment);
            builder.push(path);
            for (Field field : entry.getValue()) {
                field.setAccessible(true);
                final boolean isStatic = Modifier.isStatic(field.getModifiers());
                if (!isStatic) Objects.requireNonNull(instance, "Null instance for non-static field");
                buildConfig(builder, saveConsumer, isStatic ? null : instance, field, field.getDeclaredAnnotation(Config.class));
            }
            builder.pop(path.size());
        }
    }

    private static <T> void buildConfig(final ForgeConfigSpec.Builder builder, final BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, @Nullable Object instance, Field field, Config annotation) {
        String path = annotation.name();
        if (path.isEmpty()) {
            // https://stackoverflow.com/a/46945726
//            path = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), " "));
            path = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
        }
        String[] description = annotation.description();
        if (description.length != 0) {
            builder.comment(description);
        }
        if (annotation.worldRestart()) {
            builder.worldRestart();
        }

        Class<?> type = field.getType();
        Object defaultValue;
        try {
            defaultValue = field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (type == boolean.class) {
            addCallback(saveConsumer, builder.define(path, (boolean) defaultValue), field, instance);
        } else if (type == int.class) {
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;
            Config.IntRange intRange = field.getDeclaredAnnotation(Config.IntRange.class);
            if (intRange != null) {
                min = intRange.min();
                max = intRange.max();
            }
            addCallback(saveConsumer, builder.defineInRange(path, (int) defaultValue, min, max), field, instance);
        } else if (type == long.class) {
            long min = Long.MIN_VALUE;
            long max = Long.MAX_VALUE;
            Config.LongRange longRange = field.getDeclaredAnnotation(Config.LongRange.class);
            if (longRange != null) {
                min = longRange.min();
                max = longRange.max();
            }
            addCallback(saveConsumer, builder.defineInRange(path, (long) defaultValue, min, max), field, instance);
        } else if (type == float.class) {
            float min = Float.MIN_VALUE;
            float max = Float.MAX_VALUE;
            Config.FloatRange floatRange = field.getDeclaredAnnotation(Config.FloatRange.class);
            if (floatRange != null) {
                min = floatRange.min();
                max = floatRange.max();
            }
            final ForgeConfigSpec.DoubleValue configValue = builder.defineInRange(path, (float) defaultValue, min, max);
            saveConsumer.accept((ForgeConfigSpec.ConfigValue<T>) configValue, v -> {
                try {
                    field.set(instance, configValue.get().floatValue());
                } catch(IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (type == double.class) {
            double min = Double.MIN_VALUE;
            double max = Double.MAX_VALUE;
            Config.DoubleRange doubleRange = field.getDeclaredAnnotation(Config.DoubleRange.class);
            if (doubleRange != null) {
                min = doubleRange.min();
                max = doubleRange.max();
            }
            addCallback(saveConsumer, builder.defineInRange(path, (double) defaultValue, min, max), field, instance);
        } else if (type == String.class) {
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                description = ObjectArrays.concat(description, String.format("Allowed Values: %s", String.join(", ", allowedValues.values())));
                builder.comment(description);
                addCallback(saveConsumer, builder.define(path, (String) defaultValue, o -> {
                    if (o instanceof  String) {
                        String value = ((String) o).toLowerCase(Locale.ROOT);
                        for (String allowedValue : allowedValues.values()) {
                            if (allowedValue.toLowerCase(Locale.ROOT).equals(value)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }), field, instance);
            } else {
                addCallback(saveConsumer, builder.define(path, (String) defaultValue), field, instance);
            }
        } else if (type.isEnum()) {
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                // allowed values line handled by forge
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue, o -> {
                    String value = null;
                    if (o instanceof String) {
                        value = ((String) o);
                    } else if (o instanceof Enum) {
                        value = ((Enum<?>) o).name();
                    }
                    if (value != null) {
                        value = value.toLowerCase(Locale.ROOT);
                        for (String allowedValue : allowedValues.values()) {
                            if (allowedValue.toLowerCase(Locale.ROOT).equals(value)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }), field, instance);
            } else {
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue), field, instance);
            }
        } else if (type == List.class) {
            // TODO
        }
    }

    private static <T> void addCallback(BiConsumer<ForgeConfigSpec.ConfigValue<T>, Consumer<T>> saveConsumer, ForgeConfigSpec.ConfigValue<?> configValue, Field field, Object instance) {
        saveConsumer.accept((ForgeConfigSpec.ConfigValue<T>) configValue, v -> {
            try {
                field.set(instance, configValue.get());
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
