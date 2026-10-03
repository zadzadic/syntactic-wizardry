package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.EclipseLightRuntime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class EclipseSkyDarkenMixin {
    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void syntacticwizardry$eclipseSkyDarken(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level)(Object)this;
        int extra = EclipseLightRuntime.get(level);
        if (extra > 0) cir.setReturnValue(Math.min(15, cir.getReturnValue() + extra));
    }
}
