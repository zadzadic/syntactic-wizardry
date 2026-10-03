package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.builder.BuilderVolumeSupport;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Method;

final class ConduitAreaRender {
    private static Method drawHandleMethod;

    private ConduitAreaRender() {}

    static boolean render(RenderLevelStageEvent event,
                          ConduitPlannerState.AreaShape shape,
                          int[] bounds,
                          boolean showMoveGizmo,
                          boolean showFacingGizmo,
                          float facingYaw,
                          float facingPitch) {
        if (bounds == null) return false;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return true;

        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lines = RenderType.lines();
        VertexConsumer consumer = buffers.getBuffer(lines);

        renderShapeBlocks(pose, consumer, camera, shape, bounds);
        renderResizeHandles(pose, consumer, camera);
        if (showMoveGizmo) renderMoveGizmo(pose, consumer, camera, bounds);
        if (showFacingGizmo) renderFacingGizmo(pose, consumer, camera, bounds, facingYaw, facingPitch);

        buffers.endBatch(lines);
        return true;
    }

    static void renderGizmos(RenderLevelStageEvent event, int[] bounds, boolean showFacingGizmo, float facingYaw, float facingPitch) {
        if (bounds == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lines = RenderType.lines();
        VertexConsumer consumer = buffers.getBuffer(lines);
        renderMoveGizmo(pose, consumer, camera, bounds);
        if (showFacingGizmo) renderFacingGizmo(pose, consumer, camera, bounds, facingYaw, facingPitch);
        buffers.endBatch(lines);
    }

    private static void renderShapeBlocks(PoseStack pose, VertexConsumer consumer, Vec3 camera,
                                          ConduitPlannerState.AreaShape shape, int[] b) {
        long volume = (long)(b[3] - b[0] + 1) * (b[4] - b[1] + 1) * (b[5] - b[2] + 1);
        boolean shellOnly = volume > 4096L;

        for (int y = b[1]; y <= b[4]; y++) {
            for (int z = b[2]; z <= b[5]; z++) {
                for (int x = b[0]; x <= b[3]; x++) {
                    if (!inside(shape, b, x, y, z)) continue;
                    if (shellOnly && !shell(shape, b, x, y, z)) continue;
                    lineBox(pose, consumer, camera,
                            x, y, z, x + 1.0D, y + 1.0D, z + 1.0D,
                            0.50F, 0.78F, 1.00F, 0.55F);
                }
            }
        }
    }

    private static boolean shell(ConduitPlannerState.AreaShape shape, int[] b, int x, int y, int z) {
        return !inside(shape, b, x + 1, y, z)
                || !inside(shape, b, x - 1, y, z)
                || !inside(shape, b, x, y + 1, z)
                || !inside(shape, b, x, y - 1, z)
                || !inside(shape, b, x, y, z + 1)
                || !inside(shape, b, x, y, z - 1);
    }

    private static boolean inside(ConduitPlannerState.AreaShape shape, int[] b, int x, int y, int z) {
        if (x < b[0] || x > b[3] || y < b[1] || y > b[4] || z < b[2] || z > b[5]) return false;
        if (shape == ConduitPlannerState.AreaShape.BOX) return true;

        double cx = (b[0] + b[3] + 1.0D) * 0.5D;
        double cy = (b[1] + b[4] + 1.0D) * 0.5D;
        double cz = (b[2] + b[5] + 1.0D) * 0.5D;
        double rx = Math.max(0.5D, (b[3] - b[0] + 1) * 0.5D);
        double ry = Math.max(0.5D, (b[4] - b[1] + 1) * 0.5D);
        double rz = Math.max(0.5D, (b[5] - b[2] + 1) * 0.5D);
        double dx = (x + 0.5D - cx) / rx;
        double dz = (z + 0.5D - cz) / rz;

        if (shape == ConduitPlannerState.AreaShape.CYLINDER) {
            return dx * dx + dz * dz <= 1.0D + 1.0E-8D;
        }

        double dy = (y + 0.5D - cy) / ry;
        return dx * dx + dy * dy + dz * dz <= 1.0D + 1.0E-8D;
    }

    private static void renderResizeHandles(PoseStack pose, VertexConsumer consumer, Vec3 camera) {
        try {
            if (drawHandleMethod == null) {
                drawHandleMethod = BuilderVolumeSupport.class.getDeclaredMethod(
                        "drawHandle", Object.class, Object.class,
                        double.class, double.class, double.class, int.class, int.class);
                drawHandleMethod.setAccessible(true);
            }
            for (int axis = 0; axis < 3; axis++) {
                drawHandleMethod.invoke(null, pose, consumer, camera.x, camera.y, camera.z, axis, -1);
                drawHandleMethod.invoke(null, pose, consumer, camera.x, camera.y, camera.z, axis, 1);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void renderMoveGizmo(PoseStack pose, VertexConsumer consumer, Vec3 camera, int[] b) {
        double[] c = center(b);
        axisBox(pose, consumer, camera, c[0], c[1], c[2], 0, 1.65D, 1.0F, 0.25F, 0.25F);
        axisBox(pose, consumer, camera, c[0], c[1], c[2], 1, 1.65D, 0.25F, 1.0F, 0.25F);
        axisBox(pose, consumer, camera, c[0], c[1], c[2], 2, 1.65D, 0.25F, 0.55F, 1.0F);
    }

    private static void renderFacingGizmo(PoseStack pose, VertexConsumer consumer, Vec3 camera,
                                          int[] b, float yaw, float pitch) {
        double[] o = facingOrigin(b);
        Vec3 direction = facingDirection(yaw, pitch);
        for (int i = 0; i <= 10; i++) {
            double t = i / 10.0D * 2.2D;
            double x = o[0] + direction.x * t;
            double y = o[1] + direction.y * t;
            double z = o[2] + direction.z * t;
            double half = i == 10 ? 0.14D : 0.06D;
            lineBox(pose, consumer, camera,
                    x - half, y - half, z - half,
                    x + half, y + half, z + half,
                    1.0F, 0.72F, 0.22F, 1.0F);
        }
        lineBox(pose, consumer, camera,
                o[0] - 0.18D, o[1] - 0.18D, o[2] - 0.18D,
                o[0] + 0.18D, o[1] + 0.18D, o[2] + 0.18D,
                1.0F, 0.72F, 0.22F, 1.0F);
    }

    static double[] center(int[] b) {
        return new double[]{
                (b[0] + b[3] + 1.0D) * 0.5D,
                (b[1] + b[4] + 1.0D) * 0.5D,
                (b[2] + b[5] + 1.0D) * 0.5D
        };
    }

    static double[] facingOrigin(int[] b) {
        double[] c = center(b);
        c[1] = b[4] + 2.0D;
        return c;
    }

    static Vec3 facingDirection(float yaw, float pitch) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double cp = Math.cos(p);
        return new Vec3(-Math.sin(y) * cp, -Math.sin(p), Math.cos(y) * cp).normalize();
    }

    private static void axisBox(PoseStack pose, VertexConsumer consumer, Vec3 camera,
                                double cx, double cy, double cz, int axis, double length,
                                float r, float g, float bl) {
        double t = 0.055D;
        double minX = cx - t, minY = cy - t, minZ = cz - t;
        double maxX = cx + t, maxY = cy + t, maxZ = cz + t;
        if (axis == 0) maxX = cx + length;
        else if (axis == 1) maxY = cy + length;
        else maxZ = cz + length;
        lineBox(pose, consumer, camera, minX, minY, minZ, maxX, maxY, maxZ, r, g, bl, 1.0F);
    }

    private static void lineBox(PoseStack pose, VertexConsumer consumer, Vec3 camera,
                                double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float r, float g, float b, float a) {
        LevelRenderer.renderLineBox(pose, consumer,
                minX - camera.x, minY - camera.y, minZ - camera.z,
                maxX - camera.x, maxY - camera.y, maxZ - camera.z,
                r, g, b, a);
    }
}
