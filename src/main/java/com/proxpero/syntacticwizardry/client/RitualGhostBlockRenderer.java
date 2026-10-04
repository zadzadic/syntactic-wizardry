package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.ChalkRegistry;
import com.proxpero.syntacticwizardry.MagicLightRegistry;
import com.proxpero.syntacticwizardry.RitualStructureRules;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

final class RitualGhostBlockRenderer {
    private RitualGhostBlockRenderer() {}

    static void render(RenderLevelStageEvent event, List<PreparedRitualPreview.GhostBlock> ghosts, float alpha) {
        if (event == null || ghosts == null || ghosts.isEmpty()
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType translucent = RenderType.translucentMovingBlock();
        float clampedAlpha = Math.max(0.12F, Math.min(0.75F, alpha));

        MultiBufferSource ghostBuffers = ignored ->
                new AlphaVertexConsumer(buffers.getBuffer(translucent), clampedAlpha);

        for (PreparedRitualPreview.GhostBlock ghost : ghosts) {
            BlockState state = stateFor(ghost);
            if (state == null || state.isAir()) continue;

            BlockPos pos = ghost.pos();
            pose.pushPose();
            pose.translate(
                    pos.getX() - camera.x,
                    pos.getY() - camera.y,
                    pos.getZ() - camera.z);

            if (MagicLightRegistry.block() != null && state.is(MagicLightRegistry.block())) {
                MagicLightRenderer.renderPreview(pose, buffers, clampedAlpha);
            } else {
                mc.getBlockRenderer().renderSingleBlock(
                        state,
                        pose,
                        ghostBuffers,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        translucent);
            }
            pose.popPose();
        }

        buffers.endBatch(translucent);
    }

    private static BlockState stateFor(PreparedRitualPreview.GhostBlock ghost) {
        if (ghost.previewState() != null) return ghost.previewState();

        if (ghost.role() == RitualStructureRules.Role.CENTER) {
            return SyntacticWizardry.MATURE_CRYSTAL.get().defaultBlockState();
        }

        if (ghost.role() == RitualStructureRules.Role.RUNE) {
            return ChalkRegistry.block() == null
                    ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                    : ChalkRegistry.block().defaultBlockState();
        }

        if (ghost.role() == RitualStructureRules.Role.STRUCTURAL) {
            return Blocks.OBSIDIAN.defaultBlockState();
        }

        if (ghost.role() == RitualStructureRules.Role.FOCUS && ghost.focusMaterial() != null) {
            return switch (ghost.focusMaterial()) {
                case IRON -> Blocks.IRON_BLOCK.defaultBlockState();
                case GOLD -> Blocks.GOLD_BLOCK.defaultBlockState();
                case EMERALD -> Blocks.EMERALD_BLOCK.defaultBlockState();
                case DIAMOND -> Blocks.DIAMOND_BLOCK.defaultBlockState();
            };
        }

        return null;
    }

    private static final class AlphaVertexConsumer extends VertexConsumerWrapper {
        private final int alpha;

        private AlphaVertexConsumer(VertexConsumer parent, float alpha) {
            super(parent);
            this.alpha = Math.max(1, Math.min(255, Math.round(alpha * 255.0F)));
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int ignoredAlpha) {
            parent.setColor(red, green, blue, alpha);
            return this;
        }
    }
}
