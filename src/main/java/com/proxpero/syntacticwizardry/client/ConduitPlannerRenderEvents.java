package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ConduitPlannerRenderEvents {
    private ConduitPlannerRenderEvents() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ConduitPlannerScreen screen)) return;
        if (!screen.ritual().variableArea() || screen.selectedBlocks().isEmpty()) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lines = RenderType.lines();
        VertexConsumer consumer = buffers.getBuffer(lines);

        for (BlockPos pos : screen.selectedBlocks()) {
            lineBox(pose, consumer,
                    pos.getX() - camera.x - 0.002D,
                    pos.getY() - camera.y - 0.002D,
                    pos.getZ() - camera.z - 0.002D,
                    pos.getX() - camera.x + 1.002D,
                    pos.getY() - camera.y + 1.002D,
                    pos.getZ() - camera.z + 1.002D,
                    0.85F, 0.72F, 1.0F, 0.9F);
        }

        if (screen.areaShape() == ConduitPlannerScreen.AreaShape.BOX) {
            int[] b = screen.boxBounds();
            if (b != null) {
                lineBox(pose, consumer,
                        b[0] - camera.x - 0.02D,
                        b[1] - camera.y - 0.02D,
                        b[2] - camera.z - 0.02D,
                        b[3] - camera.x + 1.02D,
                        b[4] - camera.y + 1.02D,
                        b[5] - camera.z + 1.02D,
                        1.0F, 0.35F, 0.85F, 1.0F);
            }
        } else {
            double radius = screen.sphereRadius();
            BlockPos center = screen.conduitPos();
            double cx = center.getX() + 0.5D - camera.x;
            double cy = center.getY() + 0.5D - camera.y;
            double cz = center.getZ() + 0.5D - camera.z;
            lineBox(pose, consumer,
                    cx - radius, cy - radius, cz - radius,
                    cx + radius, cy + radius, cz + radius,
                    0.55F, 0.75F, 1.0F, 0.9F);
        }

        buffers.endBatch(lines);
    }

    private static void lineBox(PoseStack pose, VertexConsumer consumer,
                                double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        LevelRenderer.renderLineBox(pose, consumer,
                minX, minY, minZ, maxX, maxY, maxZ,
                red, green, blue, alpha);
    }
}
