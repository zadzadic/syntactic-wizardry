package com.proxpero.syntacticwizardry.mixin;

import com.arcane.magic.client.ArcaneBuilderEditEvents;
import com.proxpero.syntacticwizardry.client.ConduitPlannerState;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ArcaneBuilderEditEvents.class, remap = false)
public abstract class ConduitPlannerBuilderEditMixin {
    @Inject(method = "onRenderLevel", at = @At("HEAD"), cancellable = true, remap = false)
    private static void syntacticwizardry$conduitSelection(RenderLevelStageEvent event, CallbackInfo ci) {
        if (!ConduitPlannerState.active()) return;
        ConduitPlannerState.renderBuilderSelection(event);
        ci.cancel();
    }
}
