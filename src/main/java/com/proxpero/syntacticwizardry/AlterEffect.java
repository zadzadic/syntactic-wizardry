package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;

/** Applies vanilla status effects according to Alter's three signed properties. */
public final class AlterEffect {
    public static final int BASE_DURATION_TICKS = 30 * 20;

    private AlterEffect() {}

    public static void apply(ServerLevel level, net.minecraft.world.entity.Entity owner, ShapeResolution resolution, int strength, int speed, int toughness, int durationExtensionTicks, boolean excludeCaster) {
        int duration = BASE_DURATION_TICKS + Math.max(0, durationExtensionTicks);
        for (LivingEntity living : ResolvedTargets.living(level, resolution, excludeCaster ? owner : null)) {
            applyStrength(living, strength, duration);
            applySpeed(living, speed, duration);
            applyToughness(living, toughness, duration);
        }
    }

    private static void applyStrength(LivingEntity living, int value, int duration) {
        switch (value) {
            case -2 -> {
                add(living, MobEffects.WEAKNESS, duration, 2);
                addSigned(living, MobEffects.JUMP, duration, -2);
            }
            case -1 -> {
                add(living, MobEffects.WEAKNESS, duration, 1);
                addSigned(living, MobEffects.JUMP, duration, -1);
            }
            case 1 -> {
                add(living, MobEffects.DAMAGE_BOOST, duration, 1);
                addSigned(living, MobEffects.JUMP, duration, 1);
            }
            case 2 -> {
                add(living, MobEffects.DAMAGE_BOOST, duration, 2);
                addSigned(living, MobEffects.JUMP, duration, 2);
            }
            default -> {}
        }
    }

    private static void applySpeed(LivingEntity living, int value, int duration) {
        switch (value) {
            case -2 -> {
                add(living, MobEffects.DIG_SLOWDOWN, duration, 1);
                add(living, MobEffects.MOVEMENT_SLOWDOWN, duration, 2);
            }
            case -1 -> add(living, MobEffects.MOVEMENT_SLOWDOWN, duration, 1);
            case 1 -> add(living, MobEffects.DIG_SPEED, duration, 1);
            case 2 -> add(living, MobEffects.DIG_SPEED, duration, 2);
            default -> {}
        }
    }

    private static void applyToughness(LivingEntity living, int value, int duration) {
        switch (value) {
            case -2 -> add(living, MobEffects.WITHER, duration, 2);
            case -1 -> add(living, MobEffects.WITHER, duration, 1);
            case 1 -> add(living, MobEffects.ABSORPTION, duration, 1);
            case 2 -> add(living, MobEffects.ABSORPTION, duration, 2);
            default -> {}
        }
    }

    private static void add(LivingEntity living, Holder<MobEffect> effect, int duration, int level) {
        living.addEffect(new MobEffectInstance(effect, duration, Math.max(0, level - 1)));
    }

    private static void addSigned(LivingEntity living, Holder<MobEffect> effect, int duration, int level) {
        living.addEffect(new MobEffectInstance(effect, duration, level - 1));
    }
}
