package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Exact Catch Time structure.
 *
 * Every offset is relative directly to the clicked Mature Crystal, matching
 * the structure-validation model used by Protection and Binding.
 */
public final class CatchTimeRitualStructure {
    public record RunePlacement(BlockPos offset, int glyph) {}
    public record Detection(boolean valid, String error, CatchTimeSetting setting) {}

    public static final BlockPos LAPIS_OFFSET = new BlockPos(0, -1, 0);

    private static final List<BlockPos> STONE_BRICKS = List.of(
            new BlockPos(-2, -2, -2),
            new BlockPos(2, -2, -2),
            new BlockPos(-2, -2, 2),
            new BlockPos(2, -2, 2));

    private static final List<RunePlacement> RUNES = List.of(
            new RunePlacement(new BlockPos(-1, -1, -4), 5),
            new RunePlacement(new BlockPos(1, -1, -4), 12),
            new RunePlacement(new BlockPos(-1, -1, -3), 9),
            new RunePlacement(new BlockPos(1, -1, -3), 2),
            new RunePlacement(new BlockPos(-3, -1, -1), 8),
            new RunePlacement(new BlockPos(-2, -1, -1), 3),
            new RunePlacement(new BlockPos(2, -1, -1), 10),
            new RunePlacement(new BlockPos(3, -1, -1), 11),
            new RunePlacement(new BlockPos(-3, -1, 1), 3),
            new RunePlacement(new BlockPos(-2, -1, 1), 0),
            new RunePlacement(new BlockPos(2, -1, 1), 4),
            new RunePlacement(new BlockPos(3, -1, 1), 12),
            new RunePlacement(new BlockPos(-1, -1, 3), 1),
            new RunePlacement(new BlockPos(1, -1, 3), 5),
            new RunePlacement(new BlockPos(-1, -1, 4), 7),
            new RunePlacement(new BlockPos(1, -1, 4), 4));

    private CatchTimeRitualStructure() {}

    public static List<BlockPos> stoneBrickOffsets() {
        return STONE_BRICKS;
    }

    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static BlockPos lapisPosition(BlockPos center) {
        return center.offset(LAPIS_OFFSET);
    }

    public static BlockPos goldPosition(BlockPos center, CatchTimeSetting setting) {
        CatchTimeSetting actual = setting == null ? CatchTimeSetting.NOON : setting;
        return center.offset(actual.goldOffset());
    }

    public static Detection detect(ServerLevel level, BlockPos center) {
        if (!RitualStructureRules.isValidCenter(level.getBlockState(center))) {
            return invalid("The Center must be a Mature Crystal.");
        }

        if (!level.getBlockState(center.offset(LAPIS_OFFSET)).is(Blocks.LAPIS_BLOCK)) {
            return invalid("Catch Time requires a Lapis Block directly below the Mature Crystal.");
        }

        if (!basePatternValid(level, center)) {
            return invalid("The Catch Time ritual pattern is incomplete or incorrect.");
        }

        CatchTimeSetting found = null;
        for (CatchTimeSetting setting : CatchTimeSetting.values()) {
            if (!level.getBlockState(center.offset(setting.goldOffset())).is(Blocks.GOLD_BLOCK)) continue;
            if (found != null) {
                return invalid("Catch Time requires exactly one Gold Block next to the Lapis Block.");
            }
            found = setting;
        }

        if (found == null) {
            return invalid("Place one Gold Block directly beside the Lapis Block: East Dawn, South Noon, West Twilight, North Midnight.");
        }

        return new Detection(true, "", found);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, CatchTimeSetting expected) {
        if (expected == null) return false;
        if (!level.getBlockState(center.offset(LAPIS_OFFSET)).is(Blocks.LAPIS_BLOCK)) return false;
        if (!basePatternValid(level, center)) return false;

        for (CatchTimeSetting setting : CatchTimeSetting.values()) {
            boolean gold = level.getBlockState(center.offset(setting.goldOffset())).is(Blocks.GOLD_BLOCK);
            if (setting == expected) {
                if (!gold) return false;
            } else if (gold) {
                return false;
            }
        }

        return true;
    }

    private static boolean basePatternValid(ServerLevel level, BlockPos center) {
        for (BlockPos offset : STONE_BRICKS) {
            if (!level.getBlockState(center.offset(offset)).is(Blocks.STONE_BRICKS)) return false;
        }

        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock == null) return false;

        for (RunePlacement placement : RUNES) {
            BlockState state = level.getBlockState(center.offset(placement.offset()));
            if (!state.is(runeBlock)) return false;
            if (state.getValue(ChalkRuneBlock.FACING) != Direction.UP) return false;
            if (state.getValue(ChalkRuneBlock.COLOR) != DyeColor.BLACK) return false;
            if (state.getValue(ChalkRuneBlock.GLYPH) != placement.glyph()) return false;
        }

        return true;
    }

    private static Detection invalid(String error) {
        return new Detection(false, error, null);
    }
}