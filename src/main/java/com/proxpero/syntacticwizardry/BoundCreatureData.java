package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BoundCreatureData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_bound_creatures_v1";

    public enum Mode {
        FOLLOW("Follow"),
        WAIT("Wait"),
        WANDER("Wander"),
        GUARD("Guard");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public Mode next() {
            Mode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }

        public static Mode parse(String value) {
            if (value != null) {
                for (Mode mode : values()) {
                    if (mode.name().equalsIgnoreCase(value)) return mode;
                }
            }
            return FOLLOW;
        }
    }

    public static final class Entry {
        private final UUID entityId;
        private UUID ownerId;
        private Mode mode;
        private BlockPos anchor;
        private String anchorDimension;

        private transient UUID commandTarget;
        private transient UUID allowedTarget;
        private transient int attackCooldown;

        private Entry(
                UUID entityId,
                UUID ownerId,
                Mode mode,
                BlockPos anchor,
                String anchorDimension) {
            this.entityId = entityId;
            this.ownerId = ownerId;
            this.mode = mode == null ? Mode.FOLLOW : mode;
            this.anchor = anchor == null ? BlockPos.ZERO : anchor.immutable();
            this.anchorDimension = anchorDimension == null ? "" : anchorDimension;
        }

        public UUID entityId() { return entityId; }
        public UUID ownerId() { return ownerId; }
        public Mode mode() { return mode; }
        public BlockPos anchor() { return anchor; }
        public String anchorDimension() { return anchorDimension; }
        public UUID commandTarget() { return commandTarget; }
        public UUID allowedTarget() { return allowedTarget; }
    }

    private final Map<UUID, Entry> entries = new LinkedHashMap<>();

    private BoundCreatureData() {}

    public static Factory<BoundCreatureData> factory() {
        return new Factory<>(
                BoundCreatureData::new,
                BoundCreatureData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static BoundCreatureData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static BoundCreatureData load(CompoundTag tag, HolderLookup.Provider registries) {
        BoundCreatureData data = new BoundCreatureData();
        ListTag list = tag.getList("Bindings", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            if (!entryTag.hasUUID("Entity") || !entryTag.hasUUID("Owner")) continue;

            Entry entry = new Entry(
                    entryTag.getUUID("Entity"),
                    entryTag.getUUID("Owner"),
                    Mode.parse(entryTag.getString("Mode")),
                    BlockPos.of(entryTag.getLong("Anchor")),
                    entryTag.getString("AnchorDimension"));
            data.entries.put(entry.entityId, entry);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (Entry entry : entries.values()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Entity", entry.entityId);
            entryTag.putUUID("Owner", entry.ownerId);
            entryTag.putString("Mode", entry.mode.name());
            entryTag.putLong("Anchor", entry.anchor.asLong());
            entryTag.putString("AnchorDimension", entry.anchorDimension);
            list.add(entryTag);
        }

        tag.put("Bindings", list);
        return tag;
    }

    public Entry find(UUID entityId) {
        return entityId == null ? null : entries.get(entityId);
    }

    public boolean isBound(UUID entityId) {
        return find(entityId) != null;
    }

    public boolean isBoundTo(UUID entityId, UUID ownerId) {
        Entry entry = find(entityId);
        return entry != null && entry.ownerId.equals(ownerId);
    }

    public void bind(Mob mob, UUID ownerId) {
        if (mob == null || ownerId == null) return;

        Entry entry = new Entry(
                mob.getUUID(),
                ownerId,
                Mode.FOLLOW,
                mob.blockPosition(),
                mob.level().dimension().location().toString());

        entries.put(entry.entityId, entry);
        mob.setNoAi(false);
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.setPersistenceRequired();
        setDirty();
    }

    public Mode cycleMode(Mob mob, UUID ownerId) {
        if (mob == null || ownerId == null) return null;

        Entry entry = find(mob.getUUID());
        if (entry == null || !entry.ownerId.equals(ownerId)) return null;

        entry.mode = entry.mode.next();
        entry.commandTarget = null;
        entry.allowedTarget = null;
        entry.anchor = mob.blockPosition().immutable();
        entry.anchorDimension = mob.level().dimension().location().toString();

        if (entry.mode == Mode.WAIT) {
            mob.getNavigation().stop();
            mob.setTarget(null);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            mob.setNoAi(true);
        } else {
            mob.setNoAi(false);
        }

        setDirty();
        return entry.mode;
    }

    public int commandFollowers(ServerPlayer owner, Mob target) {
        if (owner == null || target == null || !target.isAlive()) return 0;
        if (isBoundTo(target.getUUID(), owner.getUUID())) return 0;

        int count = 0;
        for (Entry entry : entries.values()) {
            if (!entry.ownerId.equals(owner.getUUID()) || entry.mode != Mode.FOLLOW) continue;

            entry.commandTarget = target.getUUID();
            count++;
        }
        return count;
    }

    public void remove(UUID entityId) {
        if (entityId != null && entries.remove(entityId) != null) setDirty();
    }

    public boolean isAllied(UUID ownerId, LivingEntity target) {
        if (ownerId == null || target == null) return false;
        if (target.getUUID().equals(ownerId)) return true;

        Entry targetEntry = find(target.getUUID());
        return targetEntry != null && targetEntry.ownerId.equals(ownerId);
    }

    public boolean targetAllowed(Mob mob, LivingEntity target) {
        Entry entry = find(mob.getUUID());
        if (entry == null) return true;
        if (target == null) return true;
        if (isAllied(entry.ownerId, target)) return false;
        return entry.allowedTarget != null && entry.allowedTarget.equals(target.getUUID());
    }

    public void tickLevel(ServerLevel level) {
        if (level == null) return;

        for (Entry entry : new ArrayList<>(entries.values())) {
            Entity entity = level.getEntity(entry.entityId);
            if (!(entity instanceof Mob mob) || !mob.isAlive()) continue;

            mob.setPersistenceRequired();
            if (entry.attackCooldown > 0) entry.attackCooldown--;

            switch (entry.mode) {
                case FOLLOW -> tickFollow(level, mob, entry);
                case WAIT -> tickWait(mob, entry);
                case WANDER -> tickWander(level, mob, entry);
                case GUARD -> tickGuard(level, mob, entry);
            }
        }
    }

    private void tickFollow(ServerLevel level, Mob mob, Entry entry) {
        if (mob.isNoAi()) mob.setNoAi(false);

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(entry.ownerId);
        if (owner == null || owner.serverLevel() != level) {
            clearCombat(mob, entry);
            mob.getNavigation().stop();
            return;
        }

        LivingEntity target = resolveCommandTarget(level, entry);
        if (target == null) target = recentAttacker(mob, entry.ownerId);
        if (target == null) target = recentAttacker(owner, entry.ownerId);

        if (target != null) {
            combat(level, mob, entry, target);
            return;
        }

        clearCombat(mob, entry);

        double distance = mob.distanceToSqr(owner);
        if (distance > 16.0D) {
            mob.getNavigation().moveTo(owner, 1.15D);
        } else if (distance < 6.25D) {
            mob.getNavigation().stop();
        }
    }

    private void tickWait(Mob mob, Entry entry) {
        clearCombat(mob, entry);
        mob.getNavigation().stop();
        if (!mob.isNoAi()) mob.setNoAi(true);

        Vec3 motion = mob.getDeltaMovement();
        mob.setDeltaMovement(0.0D, motion.y, 0.0D);
    }

    private void tickWander(ServerLevel level, Mob mob, Entry entry) {
        if (mob.isNoAi()) mob.setNoAi(false);
        clearCombat(mob, entry);

        if (!sameDimension(level, entry)) {
            entry.anchor = mob.blockPosition().immutable();
            entry.anchorDimension = level.dimension().location().toString();
            setDirty();
        }

        if (horizontalDistanceSq(mob, entry.anchor) > 100.0D) {
            moveToAnchor(mob, entry, 1.0D);
            return;
        }

        if (!mob.getNavigation().isDone() || mob.getRandom().nextInt(80) != 0) return;

        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = mob.getRandom().nextInt(21) - 10;
            int dz = mob.getRandom().nextInt(21) - 10;
            if (dx * dx + dz * dz > 100) continue;

            BlockPos destination = entry.anchor.offset(dx, 0, dz);
            if (mob.getNavigation().moveTo(
                    destination.getX() + 0.5D,
                    destination.getY(),
                    destination.getZ() + 0.5D,
                    0.9D)) {
                return;
            }
        }
    }

    private void tickGuard(ServerLevel level, Mob mob, Entry entry) {
        if (mob.isNoAi()) mob.setNoAi(false);

        if (!sameDimension(level, entry)) {
            entry.anchor = mob.blockPosition().immutable();
            entry.anchorDimension = level.dimension().location().toString();
            setDirty();
        }

        LivingEntity hostile = nearestGuardTarget(level, mob, entry);
        if (hostile != null) {
            combat(level, mob, entry, hostile);
            return;
        }

        clearCombat(mob, entry);
        if (horizontalDistanceSq(mob, entry.anchor) > 4.0D) {
            moveToAnchor(mob, entry, 1.05D);
        } else {
            mob.getNavigation().stop();
        }
    }

    private LivingEntity resolveCommandTarget(ServerLevel level, Entry entry) {
        if (entry.commandTarget == null) return null;

        Entity target = level.getEntity(entry.commandTarget);
        if (!(target instanceof LivingEntity living)
                || !living.isAlive()
                || isAllied(entry.ownerId, living)) {
            entry.commandTarget = null;
            return null;
        }

        return living;
    }

    private LivingEntity recentAttacker(LivingEntity defended, UUID ownerId) {
        LivingEntity attacker = defended.getLastHurtByMob();
        if (attacker == null || !attacker.isAlive()) return null;
        if (defended.tickCount - defended.getLastHurtByMobTimestamp() > 100) return null;
        if (isAllied(ownerId, attacker)) return null;
        return attacker;
    }

    private LivingEntity nearestGuardTarget(ServerLevel level, Mob mob, Entry entry) {
        AABB area = mob.getBoundingBox().inflate(15.0D);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target.isAlive()
                        && target != mob
                        && target instanceof Enemy
                        && !(target instanceof Creeper)
                        && !isAllied(entry.ownerId, target));

        LivingEntity nearest = null;
        double nearestDistance = 15.0D * 15.0D;

        for (LivingEntity target : candidates) {
            double distance = mob.distanceToSqr(target);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = target;
            }
        }

        return nearest;
    }

    private void combat(ServerLevel level, Mob mob, Entry entry, LivingEntity target) {
        if (target == null || !target.isAlive() || isAllied(entry.ownerId, target)) {
            clearCombat(mob, entry);
            return;
        }

        entry.allowedTarget = target.getUUID();
        if (mob.getTarget() != target) mob.setTarget(target);
        mob.getNavigation().moveTo(target, 1.2D);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double reach = Math.max(
                1.8D,
                mob.getBbWidth() * 1.5D + target.getBbWidth() * 0.5D);

        if (entry.attackCooldown > 0 || mob.distanceToSqr(target) > reach * reach) return;

        mob.swing(InteractionHand.MAIN_HAND);
        if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            mob.doHurtTarget(target);
        } else {
            target.hurt(level.damageSources().mobAttack(mob), 2.0F);
        }
        entry.attackCooldown = 20;
    }

    private static void clearCombat(Mob mob, Entry entry) {
        entry.allowedTarget = null;
        if (mob.getTarget() != null) mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.setAggressive(false);
    }

    private static void moveToAnchor(Mob mob, Entry entry, double speed) {
        mob.getNavigation().moveTo(
                entry.anchor.getX() + 0.5D,
                entry.anchor.getY(),
                entry.anchor.getZ() + 0.5D,
                speed);
    }

    private static boolean sameDimension(ServerLevel level, Entry entry) {
        return level.dimension().location().toString().equals(entry.anchorDimension);
    }

    private static double horizontalDistanceSq(Mob mob, BlockPos pos) {
        double dx = mob.getX() - (pos.getX() + 0.5D);
        double dz = mob.getZ() - (pos.getZ() + 0.5D);
        return dx * dx + dz * dz;
    }
}
