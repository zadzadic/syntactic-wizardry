package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.EclipseLightRuntime;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class EclipseClientSkyDarkenMixin {
    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void syntacticwizardry$eclipseRenderedSkyDarken(
            float partialTick,
            CallbackInfoReturnable<Float> cir) {
        ClientLevel level = (ClientLevel)(Object)this;
        int extraDarken = EclipseLightRuntime.get(level);
        if (extraDarken <= 0) return;

        int effectiveSkyLevel = Mth.clamp(15 - level.getSkyDarken(), 0, 15);
        int baselineSkyLevel = Mth.clamp(effectiveSkyLevel + extraDarken, 0, 15);

        float targetBrightness = LightTexture.getBrightness(level.dimensionType(), effectiveSkyLevel);
        float baselineBrightness = LightTexture.getBrightness(level.dimensionType(), baselineSkyLevel);
        float scale = baselineBrightness <= 1.0E-6F ? 0.0F : targetBrightness / baselineBrightness;

        cir.setReturnValue(Mth.clamp(cir.getReturnValue() * scale, 0.0F, 1.0F));
    }
}
