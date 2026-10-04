package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public final class SummoningRitualStructure {
    private static final List<BlockPos> RUNE_OFFSETS;
    private static final List<BlockPos> STRUCTURAL_OFFSETS;

    static {
        List<BlockPos> runes = new ArrayList<>();
        List<BlockPos> structural = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                runes.add(new BlockPos(x, -1, z));
                structural.add(new BlockPos(x, -2, z));
            }
        }

        RUNE_OFFSETS = List.copyOf(runes);
        STRUCTURAL_OFFSETS = List.copyOf(structural);
    }

    private SummoningRitualStructure() {}

    public static List<BlockPos> runeOffsets() {
        return RUNE_OFFSETS;
    }

    public static List<BlockPos> structuralOffsets() {
        return STRUCTURAL_OFFSETS;
    }

    public static boolean detect(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return false;
        return activeStructureValid(level, center);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center) {
        for (BlockPos offset : RUNE_OFFSETS) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) return false;
        }

        for (BlockPos offset : STRUCTURAL_OFFSETS) {
            BlockPos pos = center.offset(offset);
            if (!RitualStructureRules.isValidStructural(level, pos)) return false;
        }

        return true;
    }

    public static boolean hasRuneSquare(ServerLevel level, BlockPos center) {
        int found = 0;
        for (BlockPos offset : RUNE_OFFSETS) {
            if (RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) found++;
        }
        return found >= 5;
    }
}
