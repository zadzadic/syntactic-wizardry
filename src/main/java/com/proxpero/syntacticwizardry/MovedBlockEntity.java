package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Temporary physical representation of blocks moved by the Move effect.
 *
 * A single moved block can still use the legacy independent physics path.
 * Area Move uses a RigidGroup: active member blocks share one translation and
 * one velocity, while terrain collision is resolved per member. Members that
 * stop remain owned by the group as fixed internal collision markers until
 * final settlement, so following layers cannot move through them.
 */
public final class MovedBlockEntity extends FallingBlockEntity {
    private static final double GRAVITY = 0.04D;
    private static final double DRAG = 0.98D;
    private static final double COLLISION_EPSILON = 1.0E-5D;
    private static final double COLLISION_BOX_EPSILON = 1.0E-4D;
    private static final double GROUP_STEP = 0.25D;
    private static final double GROUP_REST_SPEED_SQR = 0.0016D;
    private static final int GROUP_REST_TICKS = 4;
    private static final int TICKS_PER_POTENCE = 20;
    private static final int GROUP_EXTRA_LIFETIME = 600;
    private static final Map<UUID, RigidGroup> RIGID_GROUPS = new HashMap<>();

    private BlockPos originalPos = BlockPos.ZERO;
    private BlockState originalState;
    private CompoundTag originalBlockData;
    private double requiredDistance;
    private double travelledDistance;
    private int physicsTicks;
    private int maxPhysicsTicks;
    private float collisionDamage;
    private boolean customPhysics = true;
    private UUID rigidGroupId;

    public MovedBlockEntity(Level level, BlockPos pos, BlockState state, CompoundTag blockData,
                            Vec3 direction, int potence, float hardness) {
        super(EntityType.FALLING_BLOCK, level);
        CompoundTag fallingData = new CompoundTag();
        fallingData.put("BlockState", NbtUtils.writeBlockState(state));
        fallingData.putBoolean("DropItem", false);
        if (blockData != null) fallingData.put("TileEntityData", blockData.copy());
        this.readAdditionalSaveData(fallingData);
        this.blockData = blockData == null ? null : blockData.copy();
        this.originalPos = pos.immutable();
        this.originalState = state;
        this.originalBlockData = blockData == null ? null : blockData.copy();
        this.dropItem = false;
        this.setStartPos(pos);
        this.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

        int p = clampPotence(potence);
        this.requiredDistance = MoveTravelRuntime.BLOCKS_PER_POTENCE * p;
        this.maxPhysicsTicks = TICKS_PER_POTENCE * p;
        this.collisionDamage = p >= 6 ? Math.max(0.0F, hardness) + p * 0.5F : 0.0F;

        Vec3 normalized = normalize(direction);
        this.setDeltaMovement(normalized.scale(MoveTravelRuntime.blockImpulseForPotence(p)));
        this.hurtMarked = true;
    }

    public static MovedBlockEntity spawn(ServerLevel level, BlockPos pos, BlockState state, CompoundTag blockData,
                                         Vec3 direction, int potence, float hardness) {
        MovedBlockEntity entity = new MovedBlockEntity(level, pos, state, blockData, direction, potence, hardness);
        BlockState replacement = state.getFluidState().createLegacyBlock();
        if (!level.setBlock(pos, replacement, 3)) return null;
        if (level.addFreshEntity(entity)) return entity;

        restore(level, pos, state, blockData);
        return null;
    }

    /** Spawn one rigid physical object from all valid blocks in the resolved area. */
    public static boolean spawnRigidGroup(ServerLevel level, List<BlockPos> positions, Vec3 direction, int potence) {
        if (positions == null || positions.isEmpty()) return false;
        int p = clampPotence(potence);
        Vec3 normalized = normalize(direction);

        List<RigidMember> members = new ArrayList<>();
        for (BlockPos pos : new LinkedHashSet<>(positions)) {
            BlockState state = level.getBlockState(pos);
            if (!MiningTierService.canAffect(level, pos, state, p)) continue;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag blockData = blockEntity == null ? null : blockEntity.saveWithoutMetadata(level.registryAccess());
            float hardness = state.getDestroySpeed(level, pos);
            members.add(new RigidMember(pos.immutable(), state, blockData, hardness));
        }
        if (members.isEmpty()) return false;

        // Snapshot the complete structure before changing any world blocks.
        for (RigidMember member : members) {
            BlockState replacement = member.state.getFluidState().createLegacyBlock();
            if (!level.setBlock(member.originalPos, replacement, 3)) {
                restoreMembers(level, members);
                return false;
            }
        }

        UUID groupId = UUID.randomUUID();
        List<MovedBlockEntity> spawned = new ArrayList<>(members.size());
        float maxHardness = 0.0F;
        for (RigidMember member : members) {
            MovedBlockEntity entity = new MovedBlockEntity(level, member.originalPos, member.state, member.blockData,
                    normalized, p, member.hardness);
            entity.rigidGroupId = groupId;
            entity.customPhysics = false;
            entity.noPhysics = true;
            entity.setNoGravity(true);
            entity.setDeltaMovement(normalized.scale(MoveTravelRuntime.blockImpulseForPotence(p)));
            if (!level.addFreshEntity(entity)) {
                for (MovedBlockEntity other : spawned) other.discard();
                restoreMembers(level, members);
                return false;
            }
            member.entity = entity;
            spawned.add(entity);
            maxHardness = Math.max(maxHardness, Math.max(0.0F, member.hardness));
        }

        RigidGroup group = new RigidGroup(groupId, members, normalized, p, maxHardness);
        RIGID_GROUPS.put(groupId, group);
        return true;
    }

    @Override
    public void tick() {
        if (this.rigidGroupId != null && !this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            RigidGroup group = RIGID_GROUPS.get(this.rigidGroupId);
            if (group == null) {
                restoreOriginalAndDiscard();
                return;
            }
            group.tick(serverLevel);
            return;
        }

        if (!customPhysics) {
            tickVanillaLanding();
            return;
        }

        if (this.getBlockState().isAir()) {
            restoreOriginalAndDiscard();
            return;
        }

        this.time++;
        this.physicsTicks++;

        Vec3 before = this.position();
        AABB beforeBox = this.getBoundingBox();
        Vec3 requested = this.getDeltaMovement();

        this.move(MoverType.SELF, requested);

        Vec3 actual = this.position().subtract(before);
        this.travelledDistance += Math.sqrt(actual.lengthSqr());

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel && actual.lengthSqr() > 1.0E-10D) {
            handleEntityContacts(serverLevel, beforeBox.expandTowards(actual).inflate(0.10D), actual);
        }

        Vec3 velocity = clipBlockedComponents(requested, actual);
        velocity = new Vec3(velocity.x * DRAG, (velocity.y - GRAVITY) * DRAG, velocity.z * DRAG);
        this.setDeltaMovement(velocity);
        this.hurtMarked = true;

        if (this.travelledDistance + COLLISION_EPSILON >= this.requiredDistance || this.physicsTicks >= this.maxPhysicsTicks) {
            this.customPhysics = false;
        }
    }

    private void tickVanillaLanding() {
        if (this.level().isClientSide()) {
            super.tick();
            return;
        }

        ServerLevel level = (ServerLevel) this.level();
        BlockState state = this.getBlockState();
        CompoundTag data = this.blockData == null ? null : this.blockData.copy();
        BlockPos before = this.blockPosition();
        Vec3 beforePosition = this.position();
        AABB beforeBox = this.getBoundingBox();

        super.tick();

        Vec3 actual = this.position().subtract(beforePosition);
        if (actual.lengthSqr() > 1.0E-10D) {
            handleEntityContacts(level, beforeBox.expandTowards(actual).inflate(0.10D), actual);
        }

        if (!this.isRemoved()) return;
        BlockPos after = this.blockPosition();
        if (sameBlock(level, after, state) || sameBlock(level, before, state)) return;
        if (restore(level, after, state, data)) return;
        if (restore(level, before, state, data)) return;
        restore(level, this.originalPos, this.originalState, this.originalBlockData);
    }

    /**
     * Apply the physical contact of this still-airborne block for the complete
     * motion lifecycle.  Spell assistance ending does not disable its hitbox.
     * Vanilla LivingEntity hurt immunity handles repeated damage while contact
     * persists; we deliberately do not permanently blacklist an entity after
     * its first impact.
     */
    private void handleEntityContacts(ServerLevel level, AABB sweptBounds, Vec3 displacement) {
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, sweptBounds, LivingEntity::isAlive)) {
            living.move(MoverType.SELF, displacement);
            living.hurtMarked = true;
            if (this.collisionDamage > 0.0F) {
                living.hurt(level.damageSources().generic(), this.collisionDamage);
            }
        }
    }

    private void restoreOriginalAndDiscard() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            restore(serverLevel, this.originalPos, this.originalState, this.originalBlockData);
        }
        this.discard();
    }

    private static Vec3 clipBlockedComponents(Vec3 requested, Vec3 actual) {
        double x = Math.abs(actual.x - requested.x) > COLLISION_EPSILON ? 0.0D : requested.x;
        double y = Math.abs(actual.y - requested.y) > COLLISION_EPSILON ? 0.0D : requested.y;
        double z = Math.abs(actual.z - requested.z) > COLLISION_EPSILON ? 0.0D : requested.z;
        return new Vec3(x, y, z);
    }

    private static boolean sameBlock(ServerLevel level, BlockPos pos, BlockState state) {
        return state != null && level.getBlockState(pos).getBlock() == state.getBlock();
    }

    private static boolean restore(ServerLevel level, BlockPos pos, BlockState state, CompoundTag blockData) {
        if (state == null || pos == null || !isPlaceable(level.getBlockState(pos))) return false;
        if (!level.setBlock(pos, state, 3)) return false;
        if (blockData != null) {
            BlockEntity placed = level.getBlockEntity(pos);
            if (placed != null) {
                placed.loadWithComponents(blockData.copy(), level.registryAccess());
                placed.setChanged();
            }
        }
        return true;
    }

    private static boolean isPlaceable(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    private static void restoreMembers(ServerLevel level, List<RigidMember> members) {
        for (RigidMember member : members) restore(level, member.originalPos, member.state, member.blockData);
    }

    private static Vec3 normalize(Vec3 value) {
        if (value == null || value.lengthSqr() <= 1.0E-8D) return new Vec3(0.0D, 1.0D, 0.0D);
        return value.normalize();
    }

    private static int clampPotence(int potence) {
        return Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
    }

    private static final class RigidMember {
        final BlockPos originalPos;
        final BlockState state;
        final CompoundTag blockData;
        final float hardness;
        MovedBlockEntity entity;

        RigidMember(BlockPos originalPos, BlockState state, CompoundTag blockData, float hardness) {
            this.originalPos = originalPos;
            this.state = state;
            this.blockData = blockData == null ? null : blockData.copy();
            this.hardness = hardness;
        }
    }

    private static final class StoppedRigidMember {
        final RigidMember member;
        final Vec3 translation;

        StoppedRigidMember(RigidMember member, Vec3 translation) {
            this.member = member;
            this.translation = translation;
        }
    }

    /** Server-side rigid-body controller for one moved Shape occupancy. */
    private static final class RigidGroup {
        final UUID id;
        final List<RigidMember> members;
        final List<StoppedRigidMember> stoppedMembers = new ArrayList<>();
        final double requiredDistance;
        final int maxAssistedTicks;
        final int maxLifetimeTicks;
        final float collisionDamage;
        Vec3 translation = Vec3.ZERO;
        Vec3 velocity;
        double travelledDistance;
        int ticks;
        int restTicks;
        long lastGameTick = Long.MIN_VALUE;
        BlockPos lastSafeOffset = BlockPos.ZERO;

        RigidGroup(UUID id, List<RigidMember> members, Vec3 direction, int potence, float maxHardness) {
            this.id = id;
            this.members = new ArrayList<>(members);
            this.requiredDistance = MoveTravelRuntime.BLOCKS_PER_POTENCE * potence;
            this.maxAssistedTicks = TICKS_PER_POTENCE * potence;
            this.maxLifetimeTicks = this.maxAssistedTicks + GROUP_EXTRA_LIFETIME;
            this.collisionDamage = potence >= 6 ? maxHardness + potence * 0.5F : 0.0F;
            this.velocity = direction.scale(MoveTravelRuntime.blockImpulseForPotence(potence));
        }

        void tick(ServerLevel level) {
            long gameTick = level.getGameTime();
            if (lastGameTick == gameTick) return;
            lastGameTick = gameTick;
            ticks++;

            // A member that disappears unexpectedly must not freeze the rest
            // of the moving structure.  Legitimate terrain impacts remove the
            // member from this list before its FallingBlockEntity is released.
            for (int i = members.size() - 1; i >= 0; i--) {
                RigidMember member = members.get(i);
                if (member.entity != null && !member.entity.isRemoved()) continue;
                restore(level, member.originalPos, member.state, member.blockData);
                members.remove(i);
            }
            if (members.isEmpty()) {
                finish(level);
                return;
            }

            Vec3 before = translation;
            Vec3 requested = velocity;
            Vec3 actual = resolveRigidTranslation(level, this, requested);
            translation = translation.add(actual);
            travelledDistance += Math.sqrt(actual.lengthSqr());

            // Every active member may have stopped while resolving this tick.
            // Stopped members remain owned by this group until settlement.
            if (members.isEmpty()) {
                finish(level);
                return;
            }

            if (actual.lengthSqr() > 1.0E-10D) {
                handleGroupEntityContacts(level, this, before, actual);
            }

            Vec3 clipped = clipBlockedComponents(requested, actual);
            velocity = new Vec3(clipped.x * DRAG, (clipped.y - GRAVITY) * DRAG, clipped.z * DRAG);

            // Nominal spell assistance ends at the old distance/tick limit, but
            // momentum is deliberately not removed.  The rigid group simply
            // continues under its existing velocity and gravity.
            boolean assistanceDone = travelledDistance + COLLISION_EPSILON >= requiredDistance || ticks >= maxAssistedTicks;
            if (assistanceDone) {
                // Marker branch intentionally performs no braking.
            }

            updateEntities(this);
            rememberSafeOffset(level, this);

            boolean nearlyStopped = actual.lengthSqr() <= GROUP_REST_SPEED_SQR;
            boolean downwardBlocked = requested.y < -COLLISION_EPSILON && Math.abs(actual.y) <= COLLISION_EPSILON;
            if (nearlyStopped && downwardBlocked) restTicks++;
            else if (nearlyStopped && velocity.lengthSqr() <= GROUP_REST_SPEED_SQR) restTicks++;
            else restTicks = 0;

            if (restTicks >= GROUP_REST_TICKS || ticks >= maxLifetimeTicks) {
                finish(level);
            }
        }

        void finish(ServerLevel level) {
            RIGID_GROUPS.remove(id);

            Map<BlockPos, List<RigidMember>> clusters = new HashMap<>();
            BlockPos activeOffset = nearestOffset(translation);
            if (!members.isEmpty()) {
                clusters.computeIfAbsent(activeOffset, ignored -> new ArrayList<>()).addAll(members);
            }
            for (StoppedRigidMember stopped : stoppedMembers) {
                BlockPos offset = nearestOffset(stopped.translation);
                clusters.computeIfAbsent(offset, ignored -> new ArrayList<>()).add(stopped.member);
            }
            if (clusters.isEmpty()) return;

            Map.Entry<BlockPos, List<RigidMember>> main = null;
            for (Map.Entry<BlockPos, List<RigidMember>> entry : clusters.entrySet()) {
                if (main == null
                        || entry.getValue().size() > main.getValue().size()
                        || (entry.getValue().size() == main.getValue().size() && entry.getKey().equals(activeOffset))) {
                    main = entry;
                }
            }

            BlockPos mainOffset = main.getKey();
            List<RigidMember> mainMembers = main.getValue();
            boolean placed = placeAtOffset(level, mainMembers, mainOffset);
            if (!placed) {
                clearPlacedAtOffset(level, mainMembers, mainOffset);
                restoreMembers(level, mainMembers);
            }
            for (RigidMember member : mainMembers) {
                if (member.entity != null && !member.entity.isRemoved()) member.entity.discard();
            }

            for (Map.Entry<BlockPos, List<RigidMember>> entry : clusters.entrySet()) {
                if (entry.getKey().equals(mainOffset)) continue;
                for (RigidMember member : entry.getValue()) releaseStoppedMember(member);
            }
        }
    }

    private static Vec3 resolveRigidTranslation(ServerLevel level, RigidGroup group, Vec3 requested) {
        Vec3 moved = Vec3.ZERO;
        moved = moved.add(0.0D, moveAxis(level, group, moved, requested.y, 1), 0.0D);
        if (group.members.isEmpty()) return moved;
        moved = moved.add(moveAxis(level, group, moved, requested.x, 0), 0.0D, 0.0D);
        if (group.members.isEmpty()) return moved;
        moved = moved.add(0.0D, 0.0D, moveAxis(level, group, moved, requested.z, 2));
        return moved;
    }

    private static double moveAxis(ServerLevel level, RigidGroup group, Vec3 alreadyMoved, double amount, int axis) {
        if (Math.abs(amount) <= COLLISION_EPSILON || group.members.isEmpty()) return 0.0D;
        double remaining = amount;
        double moved = 0.0D;
        while (Math.abs(remaining) > COLLISION_EPSILON) {
            double step = Math.copySign(Math.min(GROUP_STEP, Math.abs(remaining)), remaining);
            Vec3 start = group.translation.add(alreadyMoved).add(axisVector(axis, moved));
            Vec3 candidate = start.add(axisVector(axis, step));

            List<RigidMember> blocked = null;
            for (RigidMember member : group.members) {
                if (canOccupy(level, group, member, candidate)) continue;
                if (blocked == null) blocked = new ArrayList<>();
                blocked.add(member);
            }

            if (blocked != null) {
                for (RigidMember member : blocked) {
                    double contact = memberContactDistance(level, group, member, start, step, axis);
                    Vec3 contactTranslation = start.add(axisVector(axis, contact));
                    stopRigidMember(group, member, contactTranslation);
                }
                group.members.removeAll(blocked);
                if (group.members.isEmpty()) break;

                // Re-evaluate this exact movement step. Newly stopped members
                // are immediate internal collision markers for the members
                // still moving in the rigid group. This prevents the next
                // layer from entering their space before Minecraft updates.
                continue;
            }

            moved += step;
            remaining -= step;
        }
        return moved;
    }

    private static Vec3 axisVector(int axis, double value) {
        return switch (axis) {
            case 0 -> new Vec3(value, 0.0D, 0.0D);
            case 1 -> new Vec3(0.0D, value, 0.0D);
            default -> new Vec3(0.0D, 0.0D, value);
        };
    }

    private static AABB memberCollisionBox(RigidMember member, Vec3 translation) {
        BlockPos p = member.originalPos;
        return new AABB(
                p.getX() + translation.x + COLLISION_BOX_EPSILON,
                p.getY() + translation.y + COLLISION_BOX_EPSILON,
                p.getZ() + translation.z + COLLISION_BOX_EPSILON,
                p.getX() + 1.0D + translation.x - COLLISION_BOX_EPSILON,
                p.getY() + 1.0D + translation.y - COLLISION_BOX_EPSILON,
                p.getZ() + 1.0D + translation.z - COLLISION_BOX_EPSILON);
    }

    private static boolean canOccupy(ServerLevel level, RigidMember member, Vec3 translation) {
        return level.noBlockCollision(null, memberCollisionBox(member, translation));
    }

    private static boolean canOccupy(ServerLevel level, RigidGroup group, RigidMember member, Vec3 translation) {
        AABB box = memberCollisionBox(member, translation);
        if (!level.noBlockCollision(null, box)) return false;
        for (StoppedRigidMember stopped : group.stoppedMembers) {
            if (memberCollisionBox(stopped.member, stopped.translation).intersects(box)) return false;
        }
        return true;
    }

    private static boolean canOccupy(ServerLevel level, List<RigidMember> members, Vec3 translation) {
        for (RigidMember member : members) {
            if (!canOccupy(level, member, translation)) return false;
        }
        return true;
    }

    /** Find the last safe point for one member along the current axis step. */
    private static double memberContactDistance(ServerLevel level, RigidGroup group, RigidMember member, Vec3 start,
                                                double step, int axis) {
        double lo = 0.0D;
        double hi = step;
        for (int i = 0; i < 10; i++) {
            double mid = (lo + hi) * 0.5D;
            Vec3 test = start.add(axisVector(axis, mid));
            if (canOccupy(level, group, member, test)) lo = mid;
            else hi = mid;
        }
        return lo;
    }

    private static void stopRigidMember(RigidGroup group, RigidMember member, Vec3 translation) {
        MovedBlockEntity entity = member.entity;
        if (entity != null && !entity.isRemoved()) {
            BlockPos p = member.originalPos;
            entity.setPos(p.getX() + 0.5D + translation.x,
                    p.getY() + translation.y,
                    p.getZ() + 0.5D + translation.z);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.noPhysics = true;
            entity.setNoGravity(true);
            entity.hurtMarked = true;
        }
        group.stoppedMembers.add(new StoppedRigidMember(member, translation));
    }

    private static void releaseStoppedMember(RigidMember member) {
        MovedBlockEntity entity = member.entity;
        if (entity == null || entity.isRemoved()) return;
        entity.rigidGroupId = null;
        entity.customPhysics = false;
        entity.noPhysics = false;
        entity.setNoGravity(false);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
    }

    private static void updateEntities(RigidGroup group) {
        for (RigidMember member : group.members) {
            MovedBlockEntity entity = member.entity;
            if (entity == null || entity.isRemoved()) continue;
            BlockPos p = member.originalPos;
            entity.setPos(p.getX() + 0.5D + group.translation.x,
                    p.getY() + group.translation.y,
                    p.getZ() + 0.5D + group.translation.z);
            entity.setDeltaMovement(group.velocity);
            entity.hurtMarked = true;
        }
    }

    /**
     * The rigid volume keeps its entity collision for its entire airborne
     * lifetime.  This is intentionally independent of the Move assistance
     * distance: launch, coasting, falling and other ballistic motion all use
     * the same swept voxel hull until the group actually settles.
     */
    private static void handleGroupEntityContacts(ServerLevel level, RigidGroup group, Vec3 before, Vec3 actual) {
        AABB bounds = groupBounds(group.members, before).minmax(groupBounds(group.members, before.add(actual))).inflate(0.10D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (!intersectsGroupSweep(living.getBoundingBox(), group.members, before, actual)) continue;
            living.move(MoverType.SELF, actual);
            living.hurtMarked = true;
            if (group.collisionDamage > 0.0F) {
                living.hurt(level.damageSources().generic(), group.collisionDamage);
            }
        }
    }

    private static AABB groupBounds(List<RigidMember> members, Vec3 translation) {
        RigidMember first = members.get(0);
        BlockPos p = first.originalPos;
        AABB result = new AABB(p.getX() + translation.x, p.getY() + translation.y, p.getZ() + translation.z,
                p.getX() + 1.0D + translation.x, p.getY() + 1.0D + translation.y, p.getZ() + 1.0D + translation.z);
        for (int i = 1; i < members.size(); i++) {
            p = members.get(i).originalPos;
            AABB box = new AABB(p.getX() + translation.x, p.getY() + translation.y, p.getZ() + translation.z,
                    p.getX() + 1.0D + translation.x, p.getY() + 1.0D + translation.y, p.getZ() + 1.0D + translation.z);
            result = result.minmax(box);
        }
        return result;
    }

    private static boolean intersectsGroupSweep(AABB target, List<RigidMember> members, Vec3 before, Vec3 actual) {
        for (RigidMember member : members) {
            BlockPos p = member.originalPos;
            AABB start = new AABB(p.getX() + before.x, p.getY() + before.y, p.getZ() + before.z,
                    p.getX() + 1.0D + before.x, p.getY() + 1.0D + before.y, p.getZ() + 1.0D + before.z);
            if (start.expandTowards(actual).intersects(target)) return true;
        }
        return false;
    }

    private static BlockPos nearestOffset(Vec3 translation) {
        return new BlockPos((int)Math.round(translation.x), (int)Math.round(translation.y), (int)Math.round(translation.z));
    }

    private static void rememberSafeOffset(ServerLevel level, RigidGroup group) {
        BlockPos candidate = nearestOffset(group.translation);
        if (canPlaceAtOffset(level, group.members, candidate)) group.lastSafeOffset = candidate;
    }

    private static boolean canPlaceAtOffset(ServerLevel level, List<RigidMember> members, BlockPos offset) {
        for (RigidMember member : members) {
            BlockPos target = member.originalPos.offset(offset.getX(), offset.getY(), offset.getZ());
            if (!isPlaceable(level.getBlockState(target))) return false;
        }
        return true;
    }

    private static boolean placeAtOffset(ServerLevel level, List<RigidMember> members, BlockPos offset) {
        if (!canPlaceAtOffset(level, members, offset)) return false;
        List<RigidMember> ordered = new ArrayList<>(members);
        ordered.sort(Comparator.comparingInt(m -> m.originalPos.getY()));

        // First place the complete rigid structure without neighbor cascades.
        for (RigidMember member : ordered) {
            BlockPos target = member.originalPos.offset(offset.getX(), offset.getY(), offset.getZ());
            if (!level.setBlock(target, member.state, 2)) return false;
        }
        // Restore block-entity data only after every supporting block exists.
        for (RigidMember member : ordered) {
            if (member.blockData == null) continue;
            BlockPos target = member.originalPos.offset(offset.getX(), offset.getY(), offset.getZ());
            BlockEntity placed = level.getBlockEntity(target);
            if (placed != null) {
                placed.loadWithComponents(member.blockData.copy(), level.registryAccess());
                placed.setChanged();
            }
        }
        // Then expose the finished structure to normal Minecraft neighbors.
        for (RigidMember member : ordered) {
            BlockPos target = member.originalPos.offset(offset.getX(), offset.getY(), offset.getZ());
            level.updateNeighborsAt(target, member.state.getBlock());
        }
        return true;
    }

    private static void clearPlacedAtOffset(ServerLevel level, List<RigidMember> members, BlockPos offset) {
        if (offset == null) return;
        for (RigidMember member : members) {
            BlockPos target = member.originalPos.offset(offset.getX(), offset.getY(), offset.getZ());
            if (level.getBlockState(target).getBlock() == member.state.getBlock()) {
                level.setBlock(target, member.state.getFluidState().createLegacyBlock(), 3);
            }
        }
    }
}
