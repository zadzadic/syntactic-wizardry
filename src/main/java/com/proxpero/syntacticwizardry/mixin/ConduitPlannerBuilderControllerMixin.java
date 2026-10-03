package com.proxpero.syntacticwizardry.mixin;

import com.arcane.magic.client.ArcaneBuilderControllerEvents;
import com.proxpero.syntacticwizardry.client.ConduitPlannerState;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ArcaneBuilderControllerEvents.class, remap = false)
public abstract class ConduitPlannerBuilderControllerMixin {
    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void syntacticwizardry$conduitTick(ClientTickEvent.Post event, CallbackInfo ci) {
        if (!ConduitPlannerState.active()) return;
        ConduitPlannerState.tick(event);
        ci.cancel();
    }

    @Inject(method = "onRenderGui", at = @At("HEAD"), cancellable = true, remap = false)
    private static void syntacticwizardry$conduitGui(RenderGuiEvent.Post event, CallbackInfo ci) {
        if (!ConduitPlannerState.active()) return;
        ConduitPlannerState.render(event.getGuiGraphics());
        ci.cancel();
    }
}
