package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class BindingRitualStructure {
    public record RunePlacement(BlockPos offset, int glyph) {}

    private static final List<RunePlacement> RUNES = List.of(
            new RunePlacement(new BlockPos(0, 0, -3), 9),
            new RunePlacement(new BlockPos(-2, 0, -2), 15),
            new RunePlacement(new BlockPos(2, 0, -2), 15),
            new RunePlacement(new BlockPos(-3, 0, 0), 10),
            new RunePlacement(new BlockPos(3, 0, 0), 8),
            new RunePlacement(new BlockPos(-2, 0, 2), 7),
            new RunePlacement(new BlockPos(2, 0, 2), 15),
            new RunePlacement(new BlockPos(0, 0, 3), 5));

    private static final List<BlockPos> LIGHTS = List.of(
            new BlockPos(-1, 0, -2),
            new BlockPos(1, 0, -2),
            new BlockPos(-2, 0, -1),
            new BlockPos(2, 0, -1),
            new BlockPos(-2, 0, 1),
            new BlockPos(2, 0, 1),
            new BlockPos(-1, 0, 2),
            new BlockPos(1, 0, 2));

    private BindingRitualStructure() {}

    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static List<BlockPos> lightOffsets() {
        return LIGHTS;
    }

    public static boolean detect(ServerLevel level, BlockPos center) {
        return RitualStructureRules.isValidCenter(level.getBlockState(center))
                && activeStructureValid(level, center);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center) {
        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock == null || MagicLightRegistry.block() == null) return false;

        for (RunePlacement placement : RUNES) {
            BlockState state = level.getBlockState(center.offset(placement.offset()));
            if (!state.is(runeBlock)) return false;
            if (state.getValue(ChalkRuneBlock.FACING) != Direction.UP) return false;
            if (state.getValue(ChalkRuneBlock.GLYPH) != placement.glyph()) return false;
        }

        for (BlockPos offset : LIGHTS) {
            if (!level.getBlockState(center.offset(offset)).is(MagicLightRegistry.block())) return false;
        }

        return true;
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos center) {
        int matches = 0;

        for (BlockPos offset : LIGHTS) {
            if (MagicLightRegistry.block() != null
                    && level.getBlockState(center.offset(offset)).is(MagicLightRegistry.block())) {
                matches++;
            }
        }

        for (RunePlacement placement : RUNES) {
            BlockState state = level.getBlockState(center.offset(placement.offset()));
            if (ChalkRegistry.block() != null
                    && state.is(ChalkRegistry.block())
                    && state.getValue(ChalkRuneBlock.GLYPH) == placement.glyph()) {
                matches++;
            }
        }

        return matches >= 6;
    }
}
