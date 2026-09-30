package com.proxpero.syntacticwizardry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Persistent Mana and Casting Experience storage plus synchronization. */
public final class ManaService {
    private static final String KEY = "syntacticwizardry_mana";
    private static final String CASTING_XP_KEY = "syntacticwizardry_casting_experience";
    private static final String MAX_MANA_BONUS_KEY = "syntacticwizardry_max_mana_bonus";

    public static final float DEFAULT_PLAYER_MANA = 50.0F;
    public static final float BASE_MAX_MANA = 50.0F;
    public static final float MAX_MANA_PER_CASTING_LEVEL = 5.0F;
    public static final float CASTING_REGEN_PER_LEVEL = 0.01F;
    private static final float EPSILON = 0.0001F;

    private ManaService() {}

    public static float getMana(Entity entity) {
        if (!(entity instanceof Player)) return 0.0F;
        CompoundTag tag = entity.getPersistentData();
        float mana = tag.contains(KEY) ? tag.getFloat(KEY) : DEFAULT_PLAYER_MANA;
        mana = clamp(entity, mana);
        if (!tag.contains(KEY) || Math.abs(mana - tag.getFloat(KEY)) > EPSILON) tag.putFloat(KEY, mana);
        return mana;
    }

    public static float getCastingExperience(Entity entity) {
        if (!(entity instanceof Player)) return 0.0F;
        return Math.max(0.0F, entity.getPersistentData().getFloat(CASTING_XP_KEY));
    }

    public static int getCastingLevel(Entity entity) {
        double xp = getCastingExperience(entity);
        return Math.max(1, (int)Math.floor((1.0D + Math.sqrt(1.0D + 4.0D * xp / 125.0D)) / 2.0D + EPSILON));
    }

    public static float cumulativeExperienceForLevel(int level) {
        int l = Math.max(1, level);
        return 125.0F * l * (l - 1);
    }

    public static float experienceToNextLevel(Entity entity) {
        int nextLevel = getCastingLevel(entity) + 1;
        return Math.max(0.0F, cumulativeExperienceForLevel(nextLevel) - getCastingExperience(entity));
    }

    public static void setCastingLevel(Entity entity, int level) {
        if (!(entity instanceof Player)) return;
        int targetLevel = Math.max(1, level);
        entity.getPersistentData().putFloat(CASTING_XP_KEY, cumulativeExperienceForLevel(targetLevel));
        float current = getMana(entity);
        float maximum = getMaxMana(entity);
        if (current > maximum) entity.getPersistentData().putFloat(KEY, maximum);
        sync(entity);
    }

    public static float getMaxMana(Entity entity) {
        if (!(entity instanceof Player)) return 0.0F;
        return BASE_MAX_MANA
                + MAX_MANA_PER_CASTING_LEVEL * (getCastingLevel(entity) - 1)
                + getMaxManaBonus(entity);
    }

    public static float getMaxManaBonus(Entity entity) {
        if (!(entity instanceof Player)) return 0.0F;
        return Math.max(0.0F, entity.getPersistentData().getFloat(MAX_MANA_BONUS_KEY));
    }

    /** Hook for equipment, enchantments and other systems that grant maximum Mana. */
    public static void setMaxManaBonus(Entity entity, float bonus) {
        if (!(entity instanceof Player)) return;
        entity.getPersistentData().putFloat(MAX_MANA_BONUS_KEY, Math.max(0.0F, bonus));
        float current = getMana(entity);
        if (current > getMaxMana(entity)) writeAndSync(entity, getMaxMana(entity));
        else sync(entity);
    }

    public static float getPassiveRegenPerSecond(Player player) {
        float thresholdRegen = (float)Math.floor(getMaxMana(player) / 100.0F) / 5.0F;
        float castingRegen = CASTING_REGEN_PER_LEVEL * (getCastingLevel(player) - 1);
        float rate = thresholdRegen + castingRegen;
        if (player.getFoodData().getSaturationLevel() > 0.0F) rate *= 2.0F;
        return rate;
    }

    /** Exact pre-floor regeneration value for progression inspection. */
    public static float getUnroundedPassiveRegenPerSecond(Player player) {
        float thresholdRegen = (getMaxMana(player) / 100.0F) / 5.0F;
        float castingRegen = CASTING_REGEN_PER_LEVEL * (getCastingLevel(player) - 1);
        float rate = thresholdRegen + castingRegen;
        if (player.getFoodData().getSaturationLevel() > 0.0F) rate *= 2.0F;
        return rate;
    }

    public static void tickRegeneration(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        float rate = getPassiveRegenPerSecond(player);
        if (rate > 0.0F && getMana(player) + EPSILON < getMaxMana(player)) addMana(player, rate);
    }

    public static boolean tryConsume(Entity entity, float amount) {
        return tryConsume(entity, amount, amount);
    }

    /**
     * Consumes the discounted/actual Mana cost while awarding Casting Experience
     * from the undiscounted raw cost.
     */
    public static boolean tryConsume(Entity entity, float amount, float rawExperienceAmount) {
        if (!(entity instanceof Player player)) return false;
        if (player.isCreative()) return true;
        float cost = Math.max(0.0F, amount);
        float before = getMana(player);
        if (before + EPSILON < cost) return false;
        writeAndSync(player, clamp(player, before - cost));
        addCastingExperience(player, Math.max(0.0F, rawExperienceAmount));
        return true;
    }

    public static void addCastingExperience(Entity entity, float amount) {
        if (!(entity instanceof Player) || amount <= 0.0F) return;
        CompoundTag tag = entity.getPersistentData();
        tag.putFloat(CASTING_XP_KEY, getCastingExperience(entity) + amount);
        sync(entity);
    }

    public static float addMana(Entity entity, float amount) {
        if (!(entity instanceof Player) || amount <= 0.0F) return 0.0F;
        float before = getMana(entity);
        float after = clamp(entity, before + amount);
        writeAndSync(entity, after);
        return after - before;
    }

    static void setClientState(Entity entity, float mana, float castingExperience, float maxManaBonus) {
        if (!(entity instanceof Player)) return;
        CompoundTag tag = entity.getPersistentData();
        tag.putFloat(CASTING_XP_KEY, Math.max(0.0F, castingExperience));
        tag.putFloat(MAX_MANA_BONUS_KEY, Math.max(0.0F, maxManaBonus));
        tag.putFloat(KEY, clamp(entity, mana));
    }

    public static void sync(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundCustomPayloadPacket(new ManaSyncPayload(
                    getMana(player), getCastingExperience(player), getMaxManaBonus(player))));
        }
    }

    private static void writeAndSync(Entity entity, float amount) {
        entity.getPersistentData().putFloat(KEY, amount);
        sync(entity);
    }

    private static float clamp(Entity entity, float amount) {
        return Math.max(0.0F, Math.min(getMaxMana(entity), amount));
    }
}
