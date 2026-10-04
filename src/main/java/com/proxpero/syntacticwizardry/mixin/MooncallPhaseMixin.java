package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.MooncallPhaseRuntime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MooncallPhaseMixin {
    @Inject(method = "getMoonPhase", at = @At("RETURN"), cancellable = true)
    private void syntacticwizardry$mooncallPhase(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level)(Object)this;
        int override = MooncallPhaseRuntime.get(level);
        if (override >= 0) cir.setReturnValue(override);
    }
}
