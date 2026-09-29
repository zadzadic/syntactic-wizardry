package com.proxpero.syntacticwizardry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Timed Flight state plus damage and movement rules shared by the Flight Effect. */
public final class FlightService {
    public static final int BASE_DURATION_TICKS = 30 * 20;
    private static final String KEY_POTENCE = "syntacticwizardry_flight_potence";
    private static final String KEY_EXPIRES = "syntacticwizardry_flight_expires";
    private static final String KEY_GRANTED_CREATIVE = "syntacticwizardry_flight_granted_creative";
    private static final String KEY_ORIGINAL_MAYFLY = "syntacticwizardry_flight_original_mayfly";
    private static final String KEY_ORIGINAL_FLYING = "syntacticwizardry_flight_original_flying";
    private static final Map<Integer,Integer> CLIENT_POTENCE = new ConcurrentHashMap<>();

    private FlightService() {}

    public static void apply(ServerLevel level, ServerPlayer player, int potence, int durationExtensionTicks) {
        if (level == null || player == null || !player.isAlive()) return;
        int resolved = Mth.clamp(potence, 1, 8);
        CompoundTag data = player.getPersistentData();
        int previous = activePotenceNoCleanup(player);
        if (previous == 8 && resolved != 8) restoreCreativeFlight(player, data);
        if (previous == 4 && resolved != 4) player.removeEffect(MobEffects.SLOW_FALLING);
        data.putInt(KEY_POTENCE, resolved);
        data.putLong(KEY_EXPIRES, level.getGameTime() + BASE_DURATION_TICKS + Math.max(0, durationExtensionTicks));
        if (resolved == 4) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, BASE_DURATION_TICKS + Math.max(0, durationExtensionTicks), 0));
        }
        if (resolved == 8) grantCreativeFlight(player, data);
        sync(player, resolved);
    }

    public static boolean canGlide(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        int potence;
        if (player.level().isClientSide) potence = CLIENT_POTENCE.getOrDefault(player.getId(), 0);
        else if (player instanceof ServerPlayer serverPlayer) potence = activePotence(serverPlayer);
        else potence = 0;
        return potence >= 5 && potence <= 7;
    }

    public static void tick(ServerPlayer player) {
        int potence = activePotence(player);
        if (potence == 0) return;
        if (potence == 8 && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    public static float reduceDamage(ServerPlayer player, DamageSource source, float amount) {
        if (player == null || source == null || amount <= 0.0F) return Math.max(0.0F, amount);
        int potence = activePotence(player);
        if (potence == 0) return amount;
        boolean fall = source.is(DamageTypes.FALL);
        boolean crash = source.is(DamageTypes.FLY_INTO_WALL);
        if (potence >= 7 && (fall || crash)) return 0.0F;
        if (potence == 6 && crash) return amount * 0.5F;
        if (fall && potence >= 1 && potence <= 3) {
            int featherLevel = potence + 4;
            float reduction = Math.min(0.80F, featherLevel * 0.12F);
            return amount * (1.0F - reduction);
        }
        return amount;
    }

    public static int activePotence(ServerPlayer player) {
        int potence = activePotenceNoCleanup(player);
        if (potence == 0) return 0;
        CompoundTag data = player.getPersistentData();
        if (player.serverLevel().getGameTime() >= data.getLong(KEY_EXPIRES)) {
            clear(player);
            return 0;
        }
        return potence;
    }

    private static int activePotenceNoCleanup(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_POTENCE) || !data.contains(KEY_EXPIRES)) return 0;
        return Mth.clamp(data.getInt(KEY_POTENCE), 1, 8);
    }

    public static void sync(ServerPlayer player) {
        sync(player, activePotence(player));
    }

    static void setClientPotence(Player player, int potence) {
        if (player == null) return;
        if (potence <= 0) CLIENT_POTENCE.remove(player.getId());
        else CLIENT_POTENCE.put(player.getId(), Mth.clamp(potence, 1, 8));
    }

    private static void sync(ServerPlayer player, int potence) {
        player.connection.send(new ClientboundCustomPayloadPacket(new FlightSyncPayload(Math.max(0, potence))));
    }

    private static void grantCreativeFlight(ServerPlayer player, CompoundTag data) {
        if (!data.getBoolean(KEY_GRANTED_CREATIVE)) {
            data.putBoolean(KEY_ORIGINAL_MAYFLY, player.getAbilities().mayfly);
            data.putBoolean(KEY_ORIGINAL_FLYING, player.getAbilities().flying);
            data.putBoolean(KEY_GRANTED_CREATIVE, true);
        }
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    private static void restoreCreativeFlight(ServerPlayer player, CompoundTag data) {
        if (!data.getBoolean(KEY_GRANTED_CREATIVE)) return;
        boolean originalMayfly = data.getBoolean(KEY_ORIGINAL_MAYFLY);
        boolean originalFlying = data.getBoolean(KEY_ORIGINAL_FLYING);
        if (player.isCreative() || player.isSpectator()) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().mayfly = originalMayfly;
            player.getAbilities().flying = originalMayfly && originalFlying;
        }
        player.onUpdateAbilities();
        data.remove(KEY_GRANTED_CREATIVE);
        data.remove(KEY_ORIGINAL_MAYFLY);
        data.remove(KEY_ORIGINAL_FLYING);
    }

    private static void clear(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int oldPotence = activePotenceNoCleanup(player);
        if (oldPotence == 8) restoreCreativeFlight(player, data);
        data.remove(KEY_POTENCE);
        data.remove(KEY_EXPIRES);
        sync(player, 0);
    }
}
