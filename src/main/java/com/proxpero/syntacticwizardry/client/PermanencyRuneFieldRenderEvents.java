package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.ChalkRegistry;
import com.proxpero.syntacticwizardry.ChalkRuneBlock;
import com.proxpero.syntacticwizardry.PermanencySyncPayload;
import com.proxpero.syntacticwizardry.ProtectionAreaShape;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PermanencyRuneFieldRenderEvents {
    private static final int MAX_COLUMNS = 144;
    private static final double RISE_HEIGHT = 1.55D;
    private static final double GROUND_OFFSET = 0.018D;

    private PermanencyRuneFieldRenderEvents() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ChalkRegistry.block() == null || PermanencyClientState.entries().isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType translucent = RenderType.translucentMovingBlock();

        boolean drew = false;
        for (PermanencyClientState.Entry state : PermanencyClientState.entries()) {
            float progress = PermanencyClientState.interpolatedProgress(level, partialTick, state);
            if (progress <= 0.001F) continue;
            drew |= renderField(
                    mc,
                    level,
                    event.getPoseStack(),
                    camera,
                    buffers,
                    translucent,
                    state.payload(),
                    progress,
                    partialTick);
        }

        if (drew) buffers.endBatch(translucent);
    }

    private static boolean renderField(
            Minecraft mc,
            ClientLevel level,
            PoseStack pose,
            Vec3 camera,
            MultiBufferSource.BufferSource buffers,
            RenderType translucent,
            PermanencySyncPayload.Entry state,
            float progress,
            float partialTick) {

        int width = Math.max(1, state.maxX() - state.minX() + 1);
        int depth = Math.max(1, state.maxZ() - state.minZ() + 1);
        int step = Math.max(2, (int)Math.ceil(Math.sqrt((width * (double)depth) / MAX_COLUMNS)));
        ProtectionAreaShape shape = ProtectionAreaShape.fromOrdinal(state.shape());

        double cx = (state.minX() + state.maxX() + 1.0D) * 0.5D;
        double cz = (state.minZ() + state.maxZ() + 1.0D) * 0.5D;
        double rx = Math.max(0.5D, width * 0.5D);
        double rz = Math.max(0.5D, depth * 0.5D);
        int scanY = state.minY();

        double time = level.getGameTime() + partialTick;
        boolean drew = false;
        int ordinal = 0;

        for (int x = state.minX(); x <= state.maxX(); x += step) {
            for (int z = state.minZ(); z <= state.maxZ(); z += step) {
                double px = x + 0.5D;
                double pz = z + 0.5D;

                if (shape != ProtectionAreaShape.BOX) {
                    double dx = (px - cx) / rx;
                    double dz = (pz - cz) / rz;
                    if (dx * dx + dz * dz > 1.0D) continue;
                }

                double ground = groundY(level, x, z, scanY);
                if (!Double.isFinite(ground)) continue;

                int hash = hash(x, z, state.center());
                int glyph = Math.floorMod(hash, 16);
                double phaseOffset = (Math.floorMod(hash >>> 4, 1000) / 1000.0D);

                for (int wave = 0; wave < 2; wave++) {
                    double phase = fract(time * 0.018D + phaseOffset + wave * 0.5D);
                    double rise = phase * RISE_HEIGHT;
                    float alpha = (float)(progress * (1.0D - phase) * 0.42D);
                    if (alpha < 0.025F) continue;

                    BlockState runeState = ChalkRegistry.block().defaultBlockState()
                            .setValue(ChalkRuneBlock.FACING, Direction.UP)
                            .setValue(ChalkRuneBlock.COLOR, DyeColor.WHITE)
                            .setValue(ChalkRuneBlock.GLYPH, glyph);

                    float yaw = (float)Math.floorMod(hash >>> 9, 360);

                    pose.pushPose();
                    pose.translate(
                            px - 0.5D - camera.x,
                            ground + GROUND_OFFSET + rise - camera.y,
                            pz - 0.5D - camera.z);
                    pose.translate(0.5D, 0.5D, 0.5D);
                    pose.mulPose(Axis.YP.rotationDegrees(yaw));
                    pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    pose.translate(-0.5D, -0.5D, -0.5D);

                    MultiBufferSource ghostBuffers = ignored ->
                            new AlphaVertexConsumer(buffers.getBuffer(translucent), alpha);
                    mc.getBlockRenderer().renderSingleBlock(
                            runeState,
                            pose,
                            ghostBuffers,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            translucent);
                    pose.popPose();
                    drew = true;
                }
                ordinal++;
                if (ordinal >= MAX_COLUMNS) return drew;
            }
        }

        return drew;
    }

    private static double groundY(ClientLevel level, int x, int z, int scanY) {
        BlockPos probe = new BlockPos(x, scanY, z);
        if (!level.hasChunkAt(probe)) return Double.NaN;

        for (int y = scanY + 8; y >= scanY - 16; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos above = pos.above();
            if (!level.getBlockState(pos).isAir()
                    && level.getBlockState(pos).isSolidRender(level, pos)
                    && level.getBlockState(above).isAir()) {
                return y + 1.0D;
            }
        }
        return scanY;
    }

    private static int hash(int x, int z, long salt) {
        long value = salt;
        value ^= (long)x * 0x9E3779B97F4A7C15L;
        value ^= (long)z * 0xC2B2AE3D27D4EB4FL;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        return (int)(value ^ (value >>> 32));
    }

    private static double fract(double value) {
        return value - Math.floor(value);
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
