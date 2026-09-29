package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * One terrain block detached by Gravity + Block Interaction.
 *
 * Each block is an independent collision object.  It keeps its hitbox and
 * follows the Gravity vector until it reaches the field center.  Attract
 * destroys the block only when the center is actually inside the block's
 * collision volume.  The object never deals collision damage.
 */
public final class GravityBlockEntity extends FallingBlockEntity {
    private static final double DRAG = 0.98D;
    private static final double COLLISION_EPSILON = 1.0E-5D;
    private static final int MAX_LIFETIME_TICKS = 1200;

    private BlockPos originalPos = BlockPos.ZERO;
    private BlockState originalState;
    private CompoundTag originalBlockData;
    private List<ItemStack> deferredDrops = List.of();
    private Vec3 gravityCenter = Vec3.ZERO;
    private boolean repel;
    private int potence = 1;
    private int physicsTicks;

    public GravityBlockEntity(Level level, BlockPos pos, BlockState state, CompoundTag blockData,
                              List<ItemStack> drops, Vec3 gravityCenter, boolean repel, int potence) {
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
        this.deferredDrops = copyDrops(drops);
        this.gravityCenter = gravityCenter == null ? new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) : gravityCenter;
        this.repel = repel;
        this.potence = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
        this.dropItem = false;
        this.setStartPos(pos);
        this.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        this.setNoGravity(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.hurtMarked = true;
    }

    public static GravityBlockEntity spawnDetached(ServerLevel level, BlockPos pos, BlockState state, CompoundTag blockData,
                                                    List<ItemStack> drops, Vec3 center, boolean repel, int potence) {
        GravityBlockEntity entity = new GravityBlockEntity(level, pos, state, blockData, drops, center, repel, potence);
        if (level.addFreshEntity(entity)) return entity;
        return null;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            // The client receives this through the vanilla falling-block type.
            // The server remains authoritative over the custom motion.
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) return;
        if (this.getBlockState().isAir()) {
            restoreWithoutBreaking(level);
            return;
        }

        this.time++;
        this.physicsTicks++;

        if (!repel && reachedCenter()) {
            breakAtCenter(level);
            return;
        }

        Vec3 bodyCenter = blockCenter();
        Vec3 vector = GravityEffect.gravityVector(this.gravityCenter, bodyCenter, this.repel);
        int effectivePotence = GravityEffect.effectivePotence(this, this.potence);
        if (effectivePotence > 0) GravityEffect.applyMovementRule(this, vector, effectivePotence);

        Vec3 before = this.position();
        AABB beforeBox = this.getBoundingBox();
        Vec3 requested = this.getDeltaMovement();
        this.move(MoverType.SELF, requested);
        Vec3 actual = this.position().subtract(before);

        if (actual.lengthSqr() > 1.0E-10D) {
            handleContacts(level, beforeBox.expandTowards(actual).inflate(0.05D), actual);
        }

        Vec3 clipped = clipBlockedComponents(requested, actual).scale(DRAG);
        this.setDeltaMovement(clipped);
        this.hurtMarked = true;

        if (!repel && reachedCenter()) {
            breakAtCenter(level);
            return;
        }

        if (this.physicsTicks >= MAX_LIFETIME_TICKS) {
            restoreWithoutBreaking(level);
        }
    }

    private boolean reachedCenter() {
        AABB box = this.getBoundingBox().inflate(0.02D);
        return this.gravityCenter.x >= box.minX && this.gravityCenter.x <= box.maxX
                && this.gravityCenter.y >= box.minY && this.gravityCenter.y <= box.maxY
                && this.gravityCenter.z >= box.minZ && this.gravityCenter.z <= box.maxZ;
    }

    private Vec3 blockCenter() {
        return this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
    }

    /** Physical contact only.  Gravity blocks never deal impact damage. */
    private void handleContacts(ServerLevel level, AABB sweptBounds, Vec3 displacement) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, sweptBounds,
                e -> e != this && !e.isRemoved() && !(e instanceof GravityBlockEntity))) {
            entity.move(MoverType.SELF, displacement);
            entity.hurtMarked = true;
        }
    }

    private void breakAtCenter(ServerLevel level) {
        BlockPos dropPos = blockPosAt(this.gravityCenter);
        BlockBreakService.spawnDrops(level, dropPos, this.deferredDrops);
        level.playSound(null, dropPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.discard();
    }

    private void restoreWithoutBreaking(ServerLevel level) {
        BlockPos current = blockPosAt(this.position());
        if (!GravityBlockRuntime.restore(level, current, this.originalState, this.originalBlockData)) {
            GravityBlockRuntime.restore(level, this.originalPos, this.originalState, this.originalBlockData);
        }
        this.discard();
    }

    private static BlockPos blockPosAt(Vec3 value) {
        return new BlockPos((int) Math.floor(value.x), (int) Math.floor(value.y), (int) Math.floor(value.z));
    }

    private static Vec3 clipBlockedComponents(Vec3 requested, Vec3 actual) {
        double x = Math.abs(actual.x - requested.x) > COLLISION_EPSILON ? 0.0D : requested.x;
        double y = Math.abs(actual.y - requested.y) > COLLISION_EPSILON ? 0.0D : requested.y;
        double z = Math.abs(actual.z - requested.z) > COLLISION_EPSILON ? 0.0D : requested.z;
        return new Vec3(x, y, z);
    }

    private static List<ItemStack> copyDrops(List<ItemStack> drops) {
        if (drops == null || drops.isEmpty()) return List.of();
        List<ItemStack> copies = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            if (drop != null && !drop.isEmpty()) copies.add(drop.copy());
        }
        return List.copyOf(copies);
    }
}
