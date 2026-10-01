package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class LightEffectRuntime {
    private record StaticEntry(long expiryTick, BlockState replacedState) {}
    private static final class MobileLightEntry {
        final Entity entity;
        final boolean originallyGlowing;
        long expiryTick;
        ServerLevel level;
        BlockPos claimedPos;

        MobileLightEntry(Entity entity, boolean originallyGlowing, long expiryTick) {
            this.entity = entity;
            this.originallyGlowing = originallyGlowing;
            this.expiryTick = expiryTick;
        }
    }
    private static final class ClaimedLight {
        final BlockState replacedState;
        int users;

        ClaimedLight(BlockState replacedState) {
            this.replacedState = replacedState;
            this.users = 1;
        }
    }

    private static final Map<ServerLevel, Map<Long, StaticEntry>> STATIC = new IdentityHashMap<>();
    private static final Map<UUID, MobileLightEntry> MOBILE = new HashMap<>();
    private static final Map<ServerLevel, Map<Long, ClaimedLight>> MOBILE_BLOCKS = new IdentityHashMap<>();

    private LightEffectRuntime() {}

    public static void placeStatic(ServerLevel level, BlockPos pos, int durationTicks) {
        if (level == null || pos == null || MagicLightRegistry.block() == null) return;

        BlockState current = level.getBlockState(pos);
        if (current.is(MagicLightRegistry.block())) return;
        if (!LightEffect.canReplace(current)) return;

        BlockState replaced = current.isAir() ? null : current;
        if (!level.setBlock(pos, MagicLightRegistry.block().defaultBlockState(), 3)) return;

        if (durationTicks <= 0) return;

        Map<Long, StaticEntry> entries = STATIC.computeIfAbsent(level, ignored -> new HashMap<>());
        long expiry = level.getGameTime() + durationTicks;
        StaticEntry previous = entries.get(pos.asLong());
        if (previous == null) entries.put(pos.asLong(), new StaticEntry(expiry, replaced));
        else entries.put(pos.asLong(), new StaticEntry(Math.max(previous.expiryTick(), expiry), previous.replacedState()));
    }

    public static void lightEntity(MinecraftServer server, Entity entity, int durationTicks) {
        if (server == null || entity == null || entity.isRemoved()) return;

        long now = entity.level().getGameTime();
        long expiry = now + Math.max(1, durationTicks);
        MobileLightEntry entry = MOBILE.get(entity.getUUID());

        if (entry == null) {
            entry = new MobileLightEntry(entity, entity.isCurrentlyGlowing(), expiry);
            MOBILE.put(entity.getUUID(), entry);
        } else {
            entry.expiryTick = Math.max(entry.expiryTick, expiry);
        }

        entity.setGlowingTag(true);
        updateMobileLight(entry);
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        tickStatic(server);
        tickMobile(server);
    }

    private static void tickStatic(MinecraftServer server) {
        Iterator<Map.Entry<ServerLevel, Map<Long, StaticEntry>>> levels = STATIC.entrySet().iterator();
        while (levels.hasNext()) {
            Map.Entry<ServerLevel, Map<Long, StaticEntry>> levelEntry = levels.next();
            ServerLevel level = levelEntry.getKey();
            if (level.getServer() != server) continue;

            long now = level.getGameTime();
            Iterator<Map.Entry<Long, StaticEntry>> entries = levelEntry.getValue().entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Long, StaticEntry> mapEntry = entries.next();
                if (now < mapEntry.getValue().expiryTick()) continue;

                BlockPos pos = BlockPos.of(mapEntry.getKey());
                restoreStatic(level, pos, mapEntry.getValue());
                entries.remove();
            }
            if (levelEntry.getValue().isEmpty()) levels.remove();
        }
    }

    private static void tickMobile(MinecraftServer server) {
        Iterator<Map.Entry<UUID, MobileLightEntry>> entries = MOBILE.entrySet().iterator();
        while (entries.hasNext()) {
            MobileLightEntry entry = entries.next().getValue();
            Entity entity = entry.entity;

            if (entity.isRemoved() || entity.level().getServer() != server) {
                releaseMobileBlock(entry);
                if (!entry.originallyGlowing && !entity.isRemoved()) entity.setGlowingTag(false);
                entries.remove();
                continue;
            }

            long now = entity.level().getGameTime();
            if (now >= entry.expiryTick) {
                releaseMobileBlock(entry);
                if (!entry.originallyGlowing) entity.setGlowingTag(false);
                entries.remove();
                continue;
            }

            if (!entity.isCurrentlyGlowing()) entity.setGlowingTag(true);
            updateMobileLight(entry);
        }
    }

    private static void updateMobileLight(MobileLightEntry entry) {
        if (!(entry.entity.level() instanceof ServerLevel level)) return;

        BlockPos desired = candidatePosition(entry.entity, level);
        if (desired == null) {
            releaseMobileBlock(entry);
            return;
        }

        if (level == entry.level && desired.equals(entry.claimedPos)) return;

        releaseMobileBlock(entry);
        claimMobileBlock(entry, level, desired);
    }

    private static BlockPos candidatePosition(Entity entity, ServerLevel level) {
        BlockPos center = BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ());
        if (canHostMobile(level, center)) return center;

        BlockPos feet = entity.blockPosition();
        if (canHostMobile(level, feet)) return feet;

        BlockPos above = feet.above();
        if (canHostMobile(level, above)) return above;

        return null;
    }

    private static boolean canHostMobile(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(MagicLightRegistry.block()) || LightEffect.canReplace(state);
    }

    private static void claimMobileBlock(MobileLightEntry entry, ServerLevel level, BlockPos pos) {
        Map<Long, ClaimedLight> claims = MOBILE_BLOCKS.computeIfAbsent(level, ignored -> new HashMap<>());
        long key = pos.asLong();
        ClaimedLight existing = claims.get(key);

        if (existing != null) {
            existing.users++;
            entry.level = level;
            entry.claimedPos = pos.immutable();
            return;
        }

        BlockState current = level.getBlockState(pos);
        if (current.is(MagicLightRegistry.block())) {
            entry.level = level;
            entry.claimedPos = null;
            return;
        }

        if (!LightEffect.canReplace(current)) return;
        BlockState replaced = current.isAir() ? null : current;
        if (!level.setBlock(pos, MagicLightRegistry.block().defaultBlockState(), 3)) return;

        claims.put(key, new ClaimedLight(replaced));
        entry.level = level;
        entry.claimedPos = pos.immutable();
    }

    private static void releaseMobileBlock(MobileLightEntry entry) {
        if (entry.level == null || entry.claimedPos == null) {
            entry.level = null;
            entry.claimedPos = null;
            return;
        }

        Map<Long, ClaimedLight> claims = MOBILE_BLOCKS.get(entry.level);
        if (claims != null) {
            long key = entry.claimedPos.asLong();
            ClaimedLight claim = claims.get(key);
            if (claim != null) {
                claim.users--;
                if (claim.users <= 0) {
                    restoreClaimed(entry.level, entry.claimedPos, claim);
                    claims.remove(key);
                }
            }
            if (claims.isEmpty()) MOBILE_BLOCKS.remove(entry.level);
        }

        entry.level = null;
        entry.claimedPos = null;
    }

    private static void restoreStatic(ServerLevel level, BlockPos pos, StaticEntry entry) {
        if (!level.getBlockState(pos).is(MagicLightRegistry.block())) return;
        BlockState restored = entry.replacedState() != null ? entry.replacedState() : Blocks.AIR.defaultBlockState();
        level.setBlock(pos, restored, 3);
    }

    private static void restoreClaimed(ServerLevel level, BlockPos pos, ClaimedLight claim) {
        if (!level.getBlockState(pos).is(MagicLightRegistry.block())) return;
        BlockState restored = claim.replacedState != null ? claim.replacedState : Blocks.AIR.defaultBlockState();
        level.setBlock(pos, restored, 3);
    }
}
