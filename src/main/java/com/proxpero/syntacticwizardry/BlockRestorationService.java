package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shared block-repair backend for Restore and future mechanics that reverse deterioration.
 * It consumes only resolved voxel occupancy and never inspects which Shape produced it.
 */
public final class BlockRestorationService {
    private static final int UPDATE_FLAGS = Block.UPDATE_ALL;

    private static final Map<Block, Block> DIRECT_RESTORATIONS = Map.ofEntries(
        Map.entry(Blocks.CRYING_OBSIDIAN, Blocks.OBSIDIAN),

        // Copper restoration deliberately uses the Arcane/Syntactic Wizardry chain:
        // oxidized -> exposed -> copper. Weathered also restores to exposed.
        Map.entry(Blocks.OXIDIZED_COPPER, Blocks.EXPOSED_COPPER),
        Map.entry(Blocks.WEATHERED_COPPER, Blocks.EXPOSED_COPPER),
        Map.entry(Blocks.EXPOSED_COPPER, Blocks.COPPER_BLOCK),

        Map.entry(Blocks.OXIDIZED_CUT_COPPER, Blocks.EXPOSED_CUT_COPPER),
        Map.entry(Blocks.WEATHERED_CUT_COPPER, Blocks.EXPOSED_CUT_COPPER),
        Map.entry(Blocks.EXPOSED_CUT_COPPER, Blocks.CUT_COPPER),

        Map.entry(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER),
        Map.entry(Blocks.WEATHERED_CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER),
        Map.entry(Blocks.EXPOSED_CHISELED_COPPER, Blocks.CHISELED_COPPER),

        Map.entry(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS),
        Map.entry(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS),
        Map.entry(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.CUT_COPPER_STAIRS),

        Map.entry(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB),
        Map.entry(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB),
        Map.entry(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.CUT_COPPER_SLAB),

        Map.entry(Blocks.OXIDIZED_COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR),
        Map.entry(Blocks.WEATHERED_COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR),
        Map.entry(Blocks.EXPOSED_COPPER_DOOR, Blocks.COPPER_DOOR),

        Map.entry(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR),
        Map.entry(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR),
        Map.entry(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.COPPER_TRAPDOOR),

        Map.entry(Blocks.OXIDIZED_COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE),
        Map.entry(Blocks.WEATHERED_COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE),
        Map.entry(Blocks.EXPOSED_COPPER_GRATE, Blocks.COPPER_GRATE),

        Map.entry(Blocks.OXIDIZED_COPPER_BULB, Blocks.EXPOSED_COPPER_BULB),
        Map.entry(Blocks.WEATHERED_COPPER_BULB, Blocks.EXPOSED_COPPER_BULB),
        Map.entry(Blocks.EXPOSED_COPPER_BULB, Blocks.COPPER_BULB),

        Map.entry(Blocks.CRACKED_STONE_BRICKS, Blocks.STONE_BRICKS),
        Map.entry(Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICKS),
        Map.entry(Blocks.CRACKED_DEEPSLATE_TILES, Blocks.DEEPSLATE_TILES),
        Map.entry(Blocks.CRACKED_NETHER_BRICKS, Blocks.NETHER_BRICKS),
        Map.entry(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS)
    );

    private BlockRestorationService() {}

    public static int restore(ServerLevel level, List<BlockPos> voxels) {
        if (voxels == null || voxels.isEmpty()) {
            return 0;
        }

        Set<Long> seen = new HashSet<>(Math.max(16, voxels.size() * 2));
        int changed = 0;
        for (BlockPos pos : voxels) {
            if (pos == null || !seen.add(pos.asLong())) {
                continue;
            }

            BlockState current = level.getBlockState(pos);
            Optional<BlockState> replacement = restoredState(current);
            if (replacement.isPresent() && replacement.get() != current && level.setBlock(pos, replacement.get(), UPDATE_FLAGS)) {
                changed++;
            }
        }
        return changed;
    }

    static Optional<BlockState> restoredState(BlockState current) {
        Block replacement = DIRECT_RESTORATIONS.get(current.getBlock());
        if (replacement == null) {
            return Optional.empty();
        }

        // Copies every property shared by the source and replacement block.
        // This preserves facing, slab type, waterlogging, door state, bulb state, etc.
        return Optional.of(replacement.withPropertiesOf(current));
    }
}
