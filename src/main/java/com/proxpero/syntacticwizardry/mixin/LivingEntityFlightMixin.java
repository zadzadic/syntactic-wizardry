package com.proxpero.syntacticwizardry.mixin;

import com.proxpero.syntacticwizardry.FlightService;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFlightMixin {
    @Shadow protected int fallFlyTicks;

    @Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
    private void syntacticwizardry$keepSpellFlight(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!self.isFallFlying() || !FlightService.canGlide(self)) return;
        if (self.onGround() || self.isPassenger() || self.hasEffect(MobEffects.LEVITATION)) return;
        this.fallFlyTicks++;
        ci.cancel();
    }
}
