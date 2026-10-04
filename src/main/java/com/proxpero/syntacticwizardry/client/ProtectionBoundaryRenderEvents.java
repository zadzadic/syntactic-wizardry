package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.proxpero.syntacticwizardry.ProtectionAreaShape;
import com.proxpero.syntacticwizardry.ProtectionRitualData;
import com.proxpero.syntacticwizardry.ProtectionSyncPayload;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ProtectionBoundaryRenderEvents {
    private static final float CORE_R = 0.72F;
    private static final float CORE_G = 0.24F;
    private static final float CORE_B = 1.00F;
    private static final double CORE_HALF_WIDTH = 0.035D;
    private static final double GLOW_HALF_WIDTH = 0.095D;
    private static final double CORE_Y_OFFSET = 0.035D;
    private static final double GLOW_Y_OFFSET = 0.024D;
    private static final int MAX_CURVE_SEGMENTS = 320;
    private static final int MAX_EDGE_SEGMENTS = 256;

    private ProtectionBoundaryRenderEvents() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ProtectionClientState.entries().isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        Matrix4f matrix = event.getPoseStack().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        boolean drew = false;

        for (ProtectionClientState.Entry state : ProtectionClientState.entries()) {
            float progress = ProtectionClientState.interpolatedProgress(level, partialTick, state);
            if (progress <= 0.001F) continue;
            drew |= renderWard(buffer, matrix, camera, level, state.payload(), progress);
        }

        MeshData mesh = buffer.build();
        if (mesh != null) BufferUploader.drawWithShader(mesh);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static boolean renderWard(BufferBuilder buffer, Matrix4f matrix, Vec3 camera, ClientLevel level,
                                      ProtectionSyncPayload.Entry state, float progress) {
        ProtectionAreaShape shape = ProtectionAreaShape.fromOrdinal(state.shape());
        double scale = Math.max(0.001D, progress);
        double cx = centerX(state);
        double cz = centerZ(state);
        double hx = halfX(state) * scale;
        double hz = halfZ(state) * scale;
        int scanY = (int)Math.floor(centerY(state));

        if (shape == ProtectionAreaShape.BOX) {
            return renderBox(buffer, matrix, camera, level, cx, cz, hx, hz, scanY);
        }
        return renderEllipse(buffer, matrix, camera, level, cx, cz, hx, hz, scanY);
    }

    private static boolean renderBox(BufferBuilder buffer, Matrix4f matrix, Vec3 camera, ClientLevel level,
                                     double cx, double cz, double hx, double hz, int scanY) {
        boolean drew = false;
        drew |= renderEdge(buffer, matrix, camera, level, cx - hx, cz - hz, cx + hx, cz - hz, scanY);
        drew |= renderEdge(buffer, matrix, camera, level, cx + hx, cz - hz, cx + hx, cz + hz, scanY);
        drew |= renderEdge(buffer, matrix, camera, level, cx + hx, cz + hz, cx - hx, cz + hz, scanY);
        drew |= renderEdge(buffer, matrix, camera, level, cx - hx, cz + hz, cx - hx, cz - hz, scanY);
        return drew;
    }

    private static boolean renderEdge(BufferBuilder buffer, Matrix4f matrix, Vec3 camera, ClientLevel level,
                                      double x0, double z0, double x1, double z1, int scanY) {
        double length = Math.hypot(x1 - x0, z1 - z0);
        int segments = clampSegments((int)Math.ceil(length), 1, MAX_EDGE_SEGMENTS);
        boolean drew = false;
        for (int i = 0; i < segments; i++) {
            double a = i / (double)segments;
            double b = (i + 1) / (double)segments;
            double ax = lerp(x0, x1, a);
            double az = lerp(z0, z1, a);
            double bx = lerp(x0, x1, b);
            double bz = lerp(z0, z1, b);
            drew |= renderGroundSegment(buffer, matrix, camera, level, ax, az, bx, bz, scanY);
        }
        return drew;
    }

    private static boolean renderEllipse(BufferBuilder buffer, Matrix4f matrix, Vec3 camera, ClientLevel level,
                                          double cx, double cz, double hx, double hz, int scanY) {
        double circumference = Math.PI * (3.0D * (hx + hz) - Math.sqrt((3.0D * hx + hz) * (hx + 3.0D * hz)));
        int segments = clampSegments((int)Math.ceil(circumference), 32, MAX_CURVE_SEGMENTS);
        boolean drew = false;
        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D * i / segments;
            double a1 = Math.PI * 2.0D * (i + 1) / segments;
            double ax = cx + Math.cos(a0) * hx;
            double az = cz + Math.sin(a0) * hz;
            double bx = cx + Math.cos(a1) * hx;
            double bz = cz + Math.sin(a1) * hz;
            drew |= renderGroundSegment(buffer, matrix, camera, level, ax, az, bx, bz, scanY);
        }
        return drew;
    }

    private static boolean renderGroundSegment(BufferBuilder buffer, Matrix4f matrix, Vec3 camera, ClientLevel level,
                                                double ax, double az, double bx, double bz, int scanY) {
        double ay = groundY(level, ax, az, scanY);
        double by = groundY(level, bx, bz, scanY);
        if (!Double.isFinite(ay) || !Double.isFinite(by)) return false;

        addRibbon(buffer, matrix, camera, ax, ay + GLOW_Y_OFFSET, az, bx, by + GLOW_Y_OFFSET, bz,
                GLOW_HALF_WIDTH, CORE_R, CORE_G, CORE_B, 0.11F);
        addRibbon(buffer, matrix, camera, ax, ay + CORE_Y_OFFSET, az, bx, by + CORE_Y_OFFSET, bz,
                CORE_HALF_WIDTH, CORE_R, CORE_G, CORE_B, 0.48F);
        return true;
    }

    private static void addRibbon(BufferBuilder buffer, Matrix4f matrix, Vec3 camera,
                                  double ax, double ay, double az, double bx, double by, double bz,
                                  double halfWidth, float r, float g, float b, float alpha) {
        double dx = bx - ax;
        double dz = bz - az;
        double length = Math.hypot(dx, dz);
        if (length <= 1.0E-6D) return;
        double nx = -dz / length * halfWidth;
        double nz = dx / length * halfWidth;

        vertex(buffer, matrix, camera, ax + nx, ay, az + nz, r, g, b, alpha);
        vertex(buffer, matrix, camera, bx + nx, by, bz + nz, r, g, b, alpha);
        vertex(buffer, matrix, camera, bx - nx, by, bz - nz, r, g, b, alpha);
        vertex(buffer, matrix, camera, ax - nx, ay, az - nz, r, g, b, alpha);
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, Vec3 camera,
                               double x, double y, double z, float r, float g, float b, float alpha) {
        buffer.addVertex(matrix,
                        (float)(x - camera.x),
                        (float)(y - camera.y),
                        (float)(z - camera.z))
                .setColor(r, g, b, alpha);
    }

    private static double groundY(ClientLevel level, double x, double z, int scanY) {
        int bx = (int)Math.floor(x);
        int bz = (int)Math.floor(z);
        BlockPos probe = new BlockPos(bx, scanY, bz);
        if (!level.hasChunkAt(probe)) return Double.NaN;

        for (int y = scanY + 8; y >= scanY - 12; y--) {
            BlockPos pos = new BlockPos(bx, y, bz);
            BlockPos above = pos.above();
            if (!level.getBlockState(pos).isAir()
                    && level.getBlockState(pos).isSolidRender(level, pos)
                    && level.getBlockState(above).isAir()) {
                return y + 1.0D;
            }
        }
        return scanY - 1.0D;
    }

    private static double centerX(ProtectionSyncPayload.Entry state) {
        if (state.customArea()) return (state.minX() + state.maxX() + 1.0D) * 0.5D;
        return BlockPos.of(state.center()).getX() + 0.5D;
    }

    private static double centerY(ProtectionSyncPayload.Entry state) {
        if (state.customArea()) return (state.minY() + state.maxY() + 1.0D) * 0.5D;
        return BlockPos.of(state.center()).getY() + 0.5D;
    }

    private static double centerZ(ProtectionSyncPayload.Entry state) {
        if (state.customArea()) return (state.minZ() + state.maxZ() + 1.0D) * 0.5D;
        return BlockPos.of(state.center()).getZ() + 0.5D;
    }

    private static double halfX(ProtectionSyncPayload.Entry state) {
        return state.customArea() ? (state.maxX() - state.minX() + 1.0D) * 0.5D : ProtectionRitualData.DEFAULT_RADIUS;
    }

    private static double halfZ(ProtectionSyncPayload.Entry state) {
        return state.customArea() ? (state.maxZ() - state.minZ() + 1.0D) * 0.5D : ProtectionRitualData.DEFAULT_RADIUS;
    }

    private static int clampSegments(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}