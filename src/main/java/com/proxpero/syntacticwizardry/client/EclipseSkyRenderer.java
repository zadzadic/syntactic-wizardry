package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class EclipseSkyRenderer {
    private static final ResourceLocation MOON =
            ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation CORONA =
            ResourceLocation.fromNamespaceAndPath(
                    SyntacticWizardry.MOD_ID, "textures/environment/eclipse_corona.png");

    private EclipseSkyRenderer() {}

    public static void render(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimensionType().hasSkyLight()) return;

        float progress = EclipseClientState.visualProgress(mc.level, partialTick);
        if (progress <= 0.001F) return;

        PoseStack pose = new PoseStack();
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(mc.level.getTimeOfDay(partialTick) * 360.0F));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        float coronaAlpha = clamp((progress - 0.45F) / 0.55F);
        if (coronaAlpha > 0.0F) {
            RenderSystem.setShaderTexture(0, CORONA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, coronaAlpha);
            drawQuad(pose.last().pose(), 43.0F, 0.0F, 100.1F, 0.0F, 0.0F, 1.0F, 1.0F);
        }

        float moonOffset = -62.0F * (1.0F - smooth(progress));
        RenderSystem.setShaderTexture(0, MOON);
        RenderSystem.setShaderColor(0.07F, 0.07F, 0.09F, 1.0F);
        drawQuad(pose.last().pose(), 31.0F, moonOffset, 99.9F, 0.0F, 0.0F, 0.25F, 0.5F);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void drawQuad(Matrix4f matrix, float size, float xOffset, float y,
                                 float u0, float v0, float u1, float v1) {
        float half = size * 0.5F;
        BufferBuilder buffer =
                Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, xOffset - half, y, -half).setUv(u0, v0);
        buffer.addVertex(matrix, xOffset + half, y, -half).setUv(u1, v0);
        buffer.addVertex(matrix, xOffset + half, y, half).setUv(u1, v1);
        buffer.addVertex(matrix, xOffset - half, y, half).setUv(u0, v1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float smooth(float value) {
        float x = clamp(value);
        return x * x * (3.0F - 2.0F * x);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
