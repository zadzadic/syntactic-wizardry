package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.client.EclipseSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class EclipseSkyRenderMixin {
    @Inject(method = "renderSky", at = @At("TAIL"))
    private void syntacticwizardry$renderEclipse(
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            float partialTick,
            Camera camera,
            boolean isFoggy,
            Runnable skyFogSetup,
            CallbackInfo ci) {
        EclipseSkyRenderer.render(partialTick);
    }
}
