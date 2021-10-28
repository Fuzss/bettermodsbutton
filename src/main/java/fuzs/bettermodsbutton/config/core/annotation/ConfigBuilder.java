package fuzs.bettermodsbutton.config.core.annotation;

import com.google.common.base.CaseFormat;
import com.google.common.collect.*;
import fuzs.bettermodsbutton.config.core.ConfigHolder;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ConfigBuilder {

    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Object target) {
        serialize(builder, target.getClass(), saveConsumer, Maps.newHashMap(), target);
    }

    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Class<?> target) {
        serialize(builder, target, saveConsumer, Maps.newHashMap(), null);
    }

    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, Object target) {
        serialize(builder, target.getClass(), saveConsumer, categoryComments, target);
    }

    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, Class<?> target) {
        serialize(builder, target, saveConsumer, categoryComments, null);
    }

    public static <T> void serialize(final ForgeConfigSpec.Builder builder, Class<? extends T> target, final ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, @Nullable T instance) {
        Multimap<List<String>, Field> pathToField = HashMultimap.create();
        for (Field field : target.getDeclaredFields()) {
            Config annotation = field.getDeclaredAnnotation(Config.class);
            if (annotation != null) {
                pathToField.put(Lists.newArrayList(annotation.category()), field);
            }
        }
        for (Map.Entry<List<String>, Collection<Field>> entry : pathToField.asMap().entrySet()) {
            final List<String> path = entry.getKey();
            List<String> currentPath = Lists.newArrayList();
            for (String category : path) {
                currentPath.add(category);
                Optional.ofNullable(categoryComments.remove(currentPath)).ifPresent(builder::comment);
                builder.push(category);
            }
            for (Field field : entry.getValue()) {
                field.setAccessible(true);
                final boolean isStatic = Modifier.isStatic(field.getModifiers());
                if (!isStatic) Objects.requireNonNull(instance, "Null instance for non-static field");
                buildConfig(builder, saveConsumer, isStatic ? null : instance, field, field.getDeclaredAnnotation(Config.class));
            }
            builder.pop(path.size());
        }
        if (!categoryComments.isEmpty()) {
            throw new RuntimeException(String.format("Unknown paths in category comments map: %s", categoryComments.keySet()));
        }
    }

    @SuppressWarnings("rawtypes")
    private static void buildConfig(final ForgeConfigSpec.Builder builder, final ConfigHolder.ConfigCallback saveConsumer, @Nullable Object instance, Field field, Config annotation) {
        String path = annotation.name();
        if (path.isEmpty()) {
            // https://stackoverflow.com/a/46945726
//            path = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), " "));
            path = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
        }
        final String[] description = annotation.description();
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
            saveConsumer.accept(configValue, v -> {
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
                builder.comment(ObjectArrays.concat(description, String.format("Allowed Values: %s", String.join(", ", allowedValues.values()))));
                addCallback(saveConsumer, builder.define(path, (String) defaultValue, o -> testAllowedValues(allowedValues, o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.define(path, (String) defaultValue), field, instance);
            }
        } else if (type.isEnum()) {
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                // allowed values line handled by forge
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue, o -> testAllowedValues(allowedValues, o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue), field, instance);
            }
        } else if (type == List.class) {
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                builder.comment(ObjectArrays.concat(description, String.format("Allowed Values: %s", String.join(", ", allowedValues.values()))));
                addCallback(saveConsumer, builder.defineList(path, (List<?>) defaultValue, o -> testAllowedValues(allowedValues, o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.defineList(path, (List<?>) defaultValue, o -> true), field, instance);
            }
        }
    }

    private static boolean testAllowedValues(Config.AllowedValues allowedValues, Object o) {
        if (o != null) {
            String value = o instanceof Enum<?> ? ((Enum<?>) o).name() : o.toString();
            for (String allowedValue : allowedValues.values()) {
                if (allowedValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addCallback(ConfigHolder.ConfigCallback addCallback, ForgeConfigSpec.ConfigValue<?> configValue, Field field, Object instance) {
        addCallback.accept(configValue, v -> {
            try {
                field.set(instance, configValue.get());
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
