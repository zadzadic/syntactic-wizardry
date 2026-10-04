package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Exact physical pattern for Catch Time, relative to the Lapis Block beneath the Mature Crystal. */
public final class CatchTimeRitualStructure {
    public record RunePlacement(BlockPos offset, int glyph) {}
    public record Detection(boolean valid, String error, CatchTimeSetting setting) {}

    private static final List<BlockPos> STONE_BRICKS = List.of(
            new BlockPos(-2, -1, -2),
            new BlockPos(2, -1, -2),
            new BlockPos(-2, -1, 2),
            new BlockPos(2, -1, 2));

    private static final List<RunePlacement> RUNES = List.of(
            new RunePlacement(new BlockPos(-1, 0, -4), 5),
            new RunePlacement(new BlockPos(1, 0, -4), 12),
            new RunePlacement(new BlockPos(-1, 0, -3), 9),
            new RunePlacement(new BlockPos(1, 0, -3), 2),
            new RunePlacement(new BlockPos(-3, 0, -1), 8),
            new RunePlacement(new BlockPos(-2, 0, -1), 3),
            new RunePlacement(new BlockPos(2, 0, -1), 10),
            new RunePlacement(new BlockPos(3, 0, -1), 11),
            new RunePlacement(new BlockPos(-3, 0, 1), 3),
            new RunePlacement(new BlockPos(-2, 0, 1), 0),
            new RunePlacement(new BlockPos(2, 0, 1), 4),
            new RunePlacement(new BlockPos(3, 0, 1), 12),
            new RunePlacement(new BlockPos(-1, 0, 3), 1),
            new RunePlacement(new BlockPos(1, 0, 3), 5),
            new RunePlacement(new BlockPos(-1, 0, 4), 7),
            new RunePlacement(new BlockPos(1, 0, 4), 4));

    private CatchTimeRitualStructure() {}

    /** Offsets are relative to the central Lapis Block. */
    public static List<BlockPos> stoneBrickOffsets() {
        return STONE_BRICKS;
    }

    /** Offsets are relative to the central Lapis Block. */
    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static BlockPos lapisCenter(BlockPos crystalCenter) {
        return crystalCenter.below();
    }

    public static BlockPos goldPosition(BlockPos crystalCenter, CatchTimeSetting setting) {
        CatchTimeSetting actual = setting == null ? CatchTimeSetting.NOON : setting;
        return lapisCenter(crystalCenter).offset(actual.goldOffset());
    }

    public static Detection detect(ServerLevel level, BlockPos crystalCenter) {
        if (!level.getBlockState(crystalCenter).is(SyntacticWizardry.MATURE_CRYSTAL.get())) {
            return invalid("The Center must be a Mature Crystal.");
        }

        BlockPos base = lapisCenter(crystalCenter);
        if (!level.getBlockState(base).is(Blocks.LAPIS_BLOCK)) {
            return invalid("Catch Time requires a Lapis Block directly below the Mature Crystal.");
        }

        if (!basePatternValid(level, base)) {
            return invalid("The Catch Time ritual pattern is incomplete.");
        }

        CatchTimeSetting found = null;
        for (CatchTimeSetting setting : CatchTimeSetting.values()) {
            if (!level.getBlockState(base.offset(setting.goldOffset())).is(Blocks.GOLD_BLOCK)) continue;
            if (found != null) {
                return invalid("Catch Time requires exactly one cardinal Gold Block.");
            }
            found = setting;
        }

        if (found == null) {
            return invalid("Place one Gold Block beside the Lapis Block: East Dawn, South Noon, West Twilight, North Midnight.");
        }

        return new Detection(true, "", found);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos crystalCenter, CatchTimeSetting expected) {
        if (expected == null) return false;
        BlockPos base = lapisCenter(crystalCenter);
        if (!level.getBlockState(base).is(Blocks.LAPIS_BLOCK)) return false;
        if (!basePatternValid(level, base)) return false;

        for (CatchTimeSetting setting : CatchTimeSetting.values()) {
            boolean gold = level.getBlockState(base.offset(setting.goldOffset())).is(Blocks.GOLD_BLOCK);
            if (setting == expected) {
                if (!gold) return false;
            } else if (gold) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos crystalCenter) {
        BlockPos base = lapisCenter(crystalCenter);
        int matches = level.getBlockState(base).is(Blocks.LAPIS_BLOCK) ? 1 : 0;
        for (BlockPos offset : STONE_BRICKS) {
            if (level.getBlockState(base.offset(offset)).is(Blocks.STONE_BRICKS)) matches++;
        }

        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock != null) {
            for (RunePlacement placement : RUNES) {
                BlockState state = level.getBlockState(base.offset(placement.offset()));
                if (state.is(runeBlock)
                        && state.getValue(ChalkRuneBlock.FACING) == Direction.UP
                        && state.getValue(ChalkRuneBlock.COLOR) == DyeColor.BLACK
                        && state.getValue(ChalkRuneBlock.GLYPH) == placement.glyph()) {
                    matches++;
                }
            }
        }
        return matches >= 6;
    }

    private static boolean basePatternValid(ServerLevel level, BlockPos base) {
        for (BlockPos offset : STONE_BRICKS) {
            if (!level.getBlockState(base.offset(offset)).is(Blocks.STONE_BRICKS)) return false;
        }

        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock == null) return false;
        for (RunePlacement placement : RUNES) {
            BlockState state = level.getBlockState(base.offset(placement.offset()));
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