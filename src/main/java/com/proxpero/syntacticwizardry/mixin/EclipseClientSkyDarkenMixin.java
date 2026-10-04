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
        float extraDarken = EclipseLightRuntime.getFloat(level);
        if (extraDarken <= 0.0001F) return;

        int integerDarken = EclipseLightRuntime.get(level);
        int effectiveSkyLevel = Mth.clamp(15 - level.getSkyDarken(), 0, 15);
        int baselineSkyLevel = Mth.clamp(effectiveSkyLevel + integerDarken, 0, 15);

        float targetSkyLevel = Mth.clamp(baselineSkyLevel - extraDarken, 0.0F, 15.0F);
        int lower = Mth.floor(targetSkyLevel);
        int upper = Math.min(15, lower + 1);
        float fraction = targetSkyLevel - lower;
        float targetBrightness = Mth.lerp(
                fraction,
                LightTexture.getBrightness(level.dimensionType(), lower),
                LightTexture.getBrightness(level.dimensionType(), upper));
        float baselineBrightness = LightTexture.getBrightness(level.dimensionType(), baselineSkyLevel);
        float scale = baselineBrightness <= 1.0E-6F ? 0.0F : targetBrightness / baselineBrightness;

        cir.setReturnValue(Mth.clamp(cir.getReturnValue() * scale, 0.0F, 1.0F));
    }
}
