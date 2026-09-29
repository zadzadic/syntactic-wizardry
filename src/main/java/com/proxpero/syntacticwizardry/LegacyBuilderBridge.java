package com.proxpero.syntacticwizardry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Thin compatibility bridge. The Builder implementation itself remains the exact Arcane 0.4.267 bytecode. */
public final class LegacyBuilderBridge {
    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();

    private LegacyBuilderBridge() {}

    public static Block newBuilderBlock(BlockBehaviour.Properties properties) {
        try {
            Class<?> type = type("com.arcane.magic.block.ArcaneBuilderBlock");
            Constructor<?> constructor = type.getConstructor(BlockBehaviour.Properties.class);
            return (Block) constructor.newInstance(properties);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Arcane Builder 0.4.267 block runtime is missing", exception);
        }
    }

    public static BlockItem newBuilderItem(Block block, Item.Properties properties) {
        try {
            Class<?> type = type("com.arcane.magic.item.ArcaneBuilderItem");
            Constructor<?> constructor = type.getConstructor(Block.class, Item.Properties.class);
            return (BlockItem) constructor.newInstance(block, properties);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Arcane Builder 0.4.267 item runtime is missing", exception);
        }
    }

    public static void invoke(String className, String methodName, Object... args) {
        invokeResult(className, methodName, args);
    }

    public static boolean invokeBoolean(String className, String methodName, Object... args) {
        Object result = invokeResult(className, methodName, args);
        return result instanceof Boolean value && value;
    }

    public static Object invokeResult(String className, String methodName, Object... args) {
        try {
            Method method = method(className, methodName, args);
            return method.invoke(null, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Arcane Builder 0.4.267 bridge failed: " + className + "." + methodName, exception);
        }
    }

    private static Class<?> type(String name) throws ClassNotFoundException {
        Class<?> cached = CLASSES.get(name);
        if (cached != null) return cached;
        Class<?> loaded = Class.forName(name);
        CLASSES.put(name, loaded);
        return loaded;
    }

    private static Method method(String className, String methodName, Object[] args) throws ReflectiveOperationException {
        StringBuilder keyBuilder = new StringBuilder(className).append('#').append(methodName).append('#').append(args.length);
        for (Object arg : args) keyBuilder.append(':').append(arg == null ? "null" : arg.getClass().getName());
        String key = keyBuilder.toString();
        Method cached = METHODS.get(key);
        if (cached != null) return cached;

        for (Method candidate : type(className).getMethods()) {
            if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != args.length) continue;
            Class<?>[] parameters = candidate.getParameterTypes();
            boolean matches = true;
            for (int i = 0; i < parameters.length; i++) {
                if (args[i] != null && !wrap(parameters[i]).isAssignableFrom(args[i].getClass())) {
                    matches = false;
                    break;
                }
            }
            if (!matches) continue;
            candidate.setAccessible(true);
            METHODS.put(key, candidate);
            return candidate;
        }
        throw new NoSuchMethodException(className + "." + methodName);
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
