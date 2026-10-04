package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Physical pattern for the Protection ritual, relative to the Mature Crystal. */
public final class ProtectionRitualStructure {
    public record RunePlacement(BlockPos offset, int glyph) {}

    private static final List<BlockPos> FOUNDATION_IRON = List.of(
            new BlockPos(-1, -2, -2),
            new BlockPos(1, -2, -2),
            new BlockPos(-2, -2, -1),
            new BlockPos(2, -2, -1),
            new BlockPos(-2, -2, 1),
            new BlockPos(2, -2, 1),
            new BlockPos(-1, -2, 2),
            new BlockPos(1, -2, 2));

    private static final List<RunePlacement> RUNES = List.of(
            new RunePlacement(new BlockPos(0, -1, -2), 2),
            new RunePlacement(new BlockPos(-1, -1, -1), 5),
            new RunePlacement(new BlockPos(1, -1, -1), 14),
            new RunePlacement(new BlockPos(-2, -1, 0), 2),
            new RunePlacement(new BlockPos(2, -1, 0), 5),
            new RunePlacement(new BlockPos(-1, -1, 1), 10),
            new RunePlacement(new BlockPos(1, -1, 1), 6),
            new RunePlacement(new BlockPos(0, -1, 2), 3));

    private static final BlockPos CENTER_IRON = new BlockPos(0, -1, 0);

    private ProtectionRitualStructure() {}

    public static List<BlockPos> foundationIronOffsets() {
        return FOUNDATION_IRON;
    }

    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static BlockPos centerIronOffset() {
        return CENTER_IRON;
    }

    public static boolean detect(ServerLevel level, BlockPos crystalCenter) {
        return RitualStructureRules.isValidCenter(level.getBlockState(crystalCenter))
                && activeStructureValid(level, crystalCenter);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos crystalCenter) {
        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock == null) return false;

        if (!level.getBlockState(crystalCenter.offset(CENTER_IRON)).is(Blocks.IRON_BLOCK)) return false;

        for (BlockPos offset : FOUNDATION_IRON) {
            if (!level.getBlockState(crystalCenter.offset(offset)).is(Blocks.IRON_BLOCK)) return false;
        }

        for (RunePlacement placement : RUNES) {
            BlockState state = level.getBlockState(crystalCenter.offset(placement.offset()));
            if (!state.is(runeBlock)) return false;
            if (state.getValue(ChalkRuneBlock.FACING) != Direction.UP) return false;
            if (state.getValue(ChalkRuneBlock.COLOR) != DyeColor.BLACK) return false;
            if (state.getValue(ChalkRuneBlock.GLYPH) != placement.glyph()) return false;
        }

        return true;
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos crystalCenter) {
        int matches = 0;

        if (level.getBlockState(crystalCenter.offset(CENTER_IRON)).is(Blocks.IRON_BLOCK)) matches++;
        for (BlockPos offset : FOUNDATION_IRON) {
            if (level.getBlockState(crystalCenter.offset(offset)).is(Blocks.IRON_BLOCK)) matches++;
        }

        ChalkRuneBlock runeBlock = ChalkRegistry.block();
        if (runeBlock != null) {
            for (RunePlacement placement : RUNES) {
                BlockState state = level.getBlockState(crystalCenter.offset(placement.offset()));
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
}