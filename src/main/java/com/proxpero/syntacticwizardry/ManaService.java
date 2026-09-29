package com.proxpero.syntacticwizardry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

/** Persistent Mana storage plus the server-to-client synchronization boundary. */
public final class ManaService {
    private static final String KEY = "syntacticwizardry_mana";
    public static final float DEFAULT_PLAYER_MANA = 50.0F;
    public static final float MAX_MANA = 50.0F;
    private static final float EPSILON = 0.0001F;

    private ManaService() {}

    public static float getMana(Entity entity) {
        if (!(entity instanceof Player)) return 0.0F;
        CompoundTag tag = entity.getPersistentData();
        float mana = tag.contains(KEY) ? tag.getFloat(KEY) : DEFAULT_PLAYER_MANA;
        mana = clamp(mana);
        if (!tag.contains(KEY) || Math.abs(mana - tag.getFloat(KEY)) > EPSILON) tag.putFloat(KEY, mana);
        return mana;
    }

    public static float getMaxMana(Entity entity) {
        return entity instanceof Player ? MAX_MANA : 0.0F;
    }

    public static boolean tryConsume(Entity entity, float amount) {
        if (!(entity instanceof Player player)) return false;
        if (player.isCreative()) return true;
        float cost = Math.max(0.0F, amount);
        float before = getMana(player);
        if (before + EPSILON < cost) return false;
        writeAndSync(player, clamp(before - cost));
        return true;
    }

    public static float addMana(Entity entity, float amount) {
        if (!(entity instanceof Player) || amount <= 0.0F) return 0.0F;
        float before = getMana(entity);
        float after = clamp(before + amount);
        writeAndSync(entity, after);
        return after - before;
    }

    static void setClientMana(Entity entity, float amount) {
        if (entity instanceof Player) entity.getPersistentData().putFloat(KEY, clamp(amount));
    }

    public static void sync(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(new ManaSyncPayload(getMana(player))));
        }
    }

    private static void writeAndSync(Entity entity, float amount) {
        entity.getPersistentData().putFloat(KEY, amount);
        sync(entity);
    }

    private static float clamp(float amount) {
        return Math.max(0.0F, Math.min(MAX_MANA, amount));
    }
}
