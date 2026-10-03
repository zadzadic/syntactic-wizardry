package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.client.ArcaneBuilderClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Reuses the proven Arcane Builder camera transform without enabling the
 * Builder's block inventory, editing toolbar, or storage controls.
 */
final class ConduitPlannerCamera {
    private static Method enterMethod;
    private static Method exitMethod;
    private static Method updateMethod;
    private static Method ensureMethod;
    private static Field heightField;

    private static boolean entered;
    private static float oldYaw;
    private static float oldPitch;

    private ConduitPlannerCamera() {}

    static boolean enter(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        try {
            oldYaw = mc.player.getYRot();
            oldPitch = mc.player.getXRot();
            method("enter", BlockPos.class).invoke(null, pos);
            ArcaneBuilderClientEvents.active = false;
            entered = true;
            return true;
        } catch (Throwable ignored) {
            ArcaneBuilderClientEvents.active = false;
            entered = false;
            return false;
        }
    }

    static void update() {
        if (!entered) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        try {
            ArcaneBuilderClientEvents.active = false;
            method("updateCameraTransform", Object.class).invoke(null, mc.player);
            method("ensureCamera", Object.class).invoke(null, mc);
        } catch (Throwable ignored) {
        }
    }

    static void rotate(double dragX, double dragY) {
        Minecraft mc = Minecraft.getInstance();
        if (!entered || mc.player == null) return;
        mc.player.setYRot(mc.player.getYRot() + (float)(dragX * 0.55D));
        mc.player.setXRot(clamp(mc.player.getXRot() + (float)(dragY * 0.55D), 18.0F, 82.0F));
        update();
    }

    static void pan(double dragX, double dragY) {
        Minecraft mc = Minecraft.getInstance();
        if (!entered || mc.player == null) return;
        double yaw = Math.toRadians(mc.player.getYRot());
        double scale = 0.035D * Math.max(5.0D, height());
        double sx = dragX * scale;
        double sy = dragY * scale;

        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);

        ArcaneBuilderClientEvents.targetX -= rightX * sx + forwardX * sy;
        ArcaneBuilderClientEvents.targetZ -= rightZ * sx + forwardZ * sy;
        update();
    }

    static void panKeyboard(double forward, double right) {
        Minecraft mc = Minecraft.getInstance();
        if (!entered || mc.player == null) return;
        double yaw = Math.toRadians(mc.player.getYRot());
        double speed = 0.35D;
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        ArcaneBuilderClientEvents.targetX += forwardX * forward * speed + rightX * right * speed;
        ArcaneBuilderClientEvents.targetZ += forwardZ * forward * speed + rightZ * right * speed;
        update();
    }

    static void zoom(double wheel) {
        if (!entered || wheel == 0.0D) return;
        try {
            Field field = height();
            double next = clamp(field.getDouble(null) - wheel * 2.0D, 5.0D, 80.0D);
            field.setDouble(null, next);
            update();
        } catch (Throwable ignored) {
        }
    }

    static void exit() {
        if (!entered) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            method("exit").invoke(null);
        } catch (Throwable ignored) {
        } finally {
            ArcaneBuilderClientEvents.active = false;
            if (mc.player != null) {
                mc.player.setYRot(oldYaw);
                mc.player.setXRot(oldPitch);
            }
            entered = false;
        }
    }

    private static double height() {
        try {
            return height().getDouble(null);
        } catch (Throwable ignored) {
            return 20.0D;
        }
    }

    private static Method method(String name, Class<?>... parameters) throws Exception {
        if ("enter".equals(name) && enterMethod != null) return enterMethod;
        if ("exit".equals(name) && exitMethod != null) return exitMethod;
        if ("updateCameraTransform".equals(name) && updateMethod != null) return updateMethod;
        if ("ensureCamera".equals(name) && ensureMethod != null) return ensureMethod;

        Method method = ArcaneBuilderClientEvents.class.getDeclaredMethod(name, parameters);
        method.setAccessible(true);
        if ("enter".equals(name)) enterMethod = method;
        else if ("exit".equals(name)) exitMethod = method;
        else if ("updateCameraTransform".equals(name)) updateMethod = method;
        else if ("ensureCamera".equals(name)) ensureMethod = method;
        return method;
    }

    private static Field height() throws Exception {
        if (heightField == null) {
            heightField = ArcaneBuilderClientEvents.class.getDeclaredField("height");
            heightField.setAccessible(true);
        }
        return heightField;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
