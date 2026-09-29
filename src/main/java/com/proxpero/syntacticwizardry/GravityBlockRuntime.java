package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Converts Gravity-affected terrain voxels into independent physical block bodies. */
public final class GravityBlockRuntime {
    private static final int UPDATE_FLAGS = 2 | 16 | 32;

    private GravityBlockRuntime() {}

    public static int spawnArea(ServerLevel level, Entity owner, ShapeResolution resolution, int potence, boolean repel) {
        if (level == null || resolution == null || resolution.voxels().isEmpty()) return 0;

        int tier = Math.max(1, Math.min(5, potence));
        List<Entry> entries = new ArrayList<>();
        Set<Long> selected = new HashSet<>();

        for (BlockPos raw : new LinkedHashSet<>(resolution.voxels())) {
            BlockPos pos = raw.immutable();
            if (!selected.add(pos.asLong())) continue;
            BlockState state = level.getBlockState(pos);
            if (!MiningTierService.canAffect(level, pos, state, tier)) continue;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag blockData = blockEntity == null ? null : blockEntity.saveWithoutMetadata(level.registryAccess());
            List<ItemStack> drops = BlockBreakService.calculateDrops(level, owner, pos, state, blockEntity, tier);
            entries.add(new Entry(pos, state, blockData, drops));
        }

        if (entries.isEmpty()) return 0;

        Set<Long> removed = new HashSet<>(entries.size() * 2);
        for (Entry entry : entries) {
            BlockState replacement = entry.state().getFluidState().createLegacyBlock();
            if (level.setBlock(entry.pos(), replacement, UPDATE_FLAGS)) removed.add(entry.pos().asLong());
        }

        int spawned = 0;
        for (Entry entry : entries) {
            if (!removed.contains(entry.pos().asLong())) continue;
            GravityBlockEntity entity = GravityBlockEntity.spawnDetached(level, entry.pos(), entry.state(), entry.blockData(),
                    entry.drops(), resolution.origin(), repel, potence);
            if (entity == null) {
                restore(level, entry);
                removed.remove(entry.pos().asLong());
            } else {
                spawned++;
            }
        }

        for (Entry entry : entries) {
            if (!removed.contains(entry.pos().asLong())) continue;
            if (isBoundary(entry.pos(), removed)) level.updateNeighborsAt(entry.pos(), entry.state().getBlock());
        }

        return spawned;
    }

    static boolean restore(ServerLevel level, BlockPos pos, BlockState state, CompoundTag blockData) {
        if (level == null || pos == null || state == null) return false;
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && existing.getFluidState().isEmpty()) return false;
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

    private static void restore(ServerLevel level, Entry entry) {
        restore(level, entry.pos(), entry.state(), entry.blockData());
    }

    private static boolean isBoundary(BlockPos pos, Set<Long> removed) {
        for (Direction direction : Direction.values()) {
            if (!removed.contains(pos.relative(direction).asLong())) return true;
        }
        return false;
    }

    private record Entry(BlockPos pos, BlockState state, CompoundTag blockData, List<ItemStack> drops) {}
}
