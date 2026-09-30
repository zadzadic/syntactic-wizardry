package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.MagicLightBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class MagicLightRenderer implements BlockEntityRenderer<MagicLightBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("syntacticwizardry", "textures/block/magic_light_sprite.png");
    private static final int FULL_BRIGHT = 0x00F000F0;

    public MagicLightRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MagicLightBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) return;

        float time = (float) (blockEntity.getLevel().getGameTime() + partialTick);
        float wave = 0.5F + 0.5F * (float) Math.sin(time * 0.10F);
        float outerScale = 0.62F + wave * 0.10F;
        float innerScale = 0.36F + wave * 0.05F;
        int outerAlpha = 112 + Math.round(wave * 36.0F);
        int innerAlpha = 184 + Math.round(wave * 40.0F);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderQuad(poseStack, vc, outerScale, outerAlpha);
        renderQuad(poseStack, vc, innerScale, innerAlpha);

        poseStack.popPose();
    }

    private static void renderQuad(PoseStack poseStack, VertexConsumer vc, float halfSize, int alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        float min = -halfSize;
        float max = halfSize;
        int color = 255;

        vc.addVertex(matrix4f, min, min, 0.0F).setColor(color, color, color, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix4f, max, min, 0.0F).setColor(color, color, color, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix4f, max, max, 0.0F).setColor(color, color, color, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, 1.0F);
        vc.addVertex(matrix4f, min, max, 0.0F).setColor(color, color, color, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 0.0F, 1.0F);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
