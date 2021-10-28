package fuzs.bettermodsbutton.config.core.annotation;

import com.google.common.base.CaseFormat;
import com.google.common.collect.*;
import fuzs.bettermodsbutton.config.core.ConfigHolder;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * build config values from given class, values are marked via {@link Config} annotation
 */
public class ConfigBuilder {

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param target object instance
     */
    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, @Nonnull Object target) {
        serialize(builder, saveConsumer, Maps.newHashMap(), target.getClass(), target);
    }

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param target target class
     */
    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Class<?> target) {
        serialize(builder, saveConsumer, Maps.newHashMap(), target, null);
    }

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param categoryComments level comments for categories
     * @param target object instance
     */
    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, @Nonnull Object target) {
        serialize(builder, saveConsumer, categoryComments, target.getClass(), target);
    }

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param categoryComments level comments for categories
     * @param target target class
     */
    public static void serialize(ForgeConfigSpec.Builder builder, ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, Class<?> target) {
        serialize(builder, saveConsumer, categoryComments, target, null);
    }

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param categoryComments level comments for categories
     * @param target target class
     * @param instance object instance, null when static
     * @param <T> <code>instance</code> type
     */
    public static <T> void serialize(final ForgeConfigSpec.Builder builder, final ConfigHolder.ConfigCallback saveConsumer, Map<List<String>, String[]> categoryComments, Class<? extends T> target, @Nullable T instance) {
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

    /**
     * @param builder forge builder for creating forge config values, setting comments, etc.
     * @param saveConsumer callback
     * @param instance object instance, null when static
     * @param field field to save to
     * @param annotation config annotation for config value data
     */
    @SuppressWarnings("rawtypes")
    private static void buildConfig(final ForgeConfigSpec.Builder builder, final ConfigHolder.ConfigCallback saveConsumer, @Nullable Object instance, Field field, Config annotation) {
        String path = annotation.name();
        if (path.isEmpty()) {
            // https://stackoverflow.com/a/46945726
//            path = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), " "));
            path = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
        }
        if (annotation.description().length != 0) {
            builder.comment(annotation.description());
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
                builder.comment(ObjectArrays.concat(annotation.description(), String.format("Allowed Values: %s", String.join(", ", allowedValues.values()))));
                addCallback(saveConsumer, builder.define(path, (String) defaultValue, o -> testAllowedValues(allowedValues.values(), o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.define(path, (String) defaultValue), field, instance);
            }
        } else if (type.isEnum()) {
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                // allowed values line handled by forge
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue, o -> testAllowedValues(allowedValues.values(), o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.defineEnum(path, (Enum) defaultValue), field, instance);
            }
        } else if (type == List.class) {
            // currently, only supports a predicate for string and enum lists, might also want to add range check for number values
            Config.AllowedValues allowedValues = field.getDeclaredAnnotation(Config.AllowedValues.class);
            if (allowedValues != null && allowedValues.values().length != 0) {
                builder.comment(ObjectArrays.concat(annotation.description(), String.format("Allowed Values: %s", String.join(", ", allowedValues.values()))));
                addCallback(saveConsumer, builder.defineList(path, (List<?>) defaultValue, o -> testAllowedValues(allowedValues.values(), o)), field, instance);
            } else {
                addCallback(saveConsumer, builder.defineList(path, (List<?>) defaultValue, o -> true), field, instance);
            }
        }
    }

    /**
     * @param allowedValues allowed values array
     * @param o object to test
     * @return does <code>allowedValues</code> contain <code>o</code> tested by comparing strings
     */
    private static boolean testAllowedValues(String[] allowedValues, Object o) {
        if (o != null) {
            String value = o instanceof Enum<?> ? ((Enum<?>) o).name() : o.toString();
            for (String allowedValue : allowedValues) {
                if (allowedValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param saveCallback callback
     * @param configValue forge config value
     * @param field field to save to
     * @param instance object instance, null when static
     */
    private static void addCallback(ConfigHolder.ConfigCallback saveCallback, ForgeConfigSpec.ConfigValue<?> configValue, Field field, @Nullable Object instance) {
        saveCallback.accept(configValue, v -> {
            try {
                field.set(instance, configValue.get());
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
