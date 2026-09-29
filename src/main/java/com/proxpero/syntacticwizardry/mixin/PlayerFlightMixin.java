package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.FlightService;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerFlightMixin {
    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void syntacticwizardry$startSpellFlight(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player)(Object)this;
        if (!FlightService.canGlide(self)) return;
        if (self.onGround() || self.isFallFlying() || self.isPassenger() || self.isInWater() || self.hasEffect(MobEffects.LEVITATION)) return;
        self.startFallFlying();
        cir.setReturnValue(true);
    }
}
