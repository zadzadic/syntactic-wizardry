package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.EclipseLightRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class EclipseRawBrightnessMixin {
    @Inject(method = "getRawBrightness", at = @At("RETURN"), cancellable = true)
    private void syntacticwizardry$eclipseRawBrightness(
            BlockPos pos,
            int ambientDarkening,
            CallbackInfoReturnable<Integer> cir) {
        Level level = (Level)(Object)this;
        if (EclipseLightRuntime.get(level) <= 0) return;

        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        int effectiveSky = Math.max(0, skyLight - level.getSkyDarken());
        cir.setReturnValue(Math.max(blockLight, effectiveSky));
    }
}
