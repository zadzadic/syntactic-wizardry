package com.proxpero.syntacticwizardry.mixin;

import com.arcane.magic.builder.BuilderVolumeSupport;
import com.proxpero.syntacticwizardry.client.ConduitPlannerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BuilderVolumeSupport.class, remap = false)
public abstract class ConduitPlannerVolumeMixin {
    @Inject(method = "commitBounds", at = @At("HEAD"), cancellable = true, remap = false)
    private static void syntacticwizardry$commitVirtualBounds(Object minecraft, CallbackInfo ci) {
        if (!ConduitPlannerState.active() || !ConduitPlannerState.ritual().variableArea()) return;
        ConduitPlannerState.commitVirtualBounds();
        ci.cancel();
    }
}
