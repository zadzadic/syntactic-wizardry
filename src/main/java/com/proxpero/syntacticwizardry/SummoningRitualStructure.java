package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public final class SummoningRitualStructure {
    private static final List<BlockPos> RUNE_OFFSETS;
    private static final List<BlockPos> STRUCTURAL_OFFSETS;
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN;
    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN;

    static {
        List<BlockPos> runes = new ArrayList<>();
        List<BlockPos> structural = new ArrayList<>();
        List<RitualStructureRules.PatternSlot> active = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    BlockPos rune = new BlockPos(x, 0, z);
                    runes.add(rune);
                    active.add(new RitualStructureRules.PatternSlot(rune, RitualStructureRules.Role.RUNE));
                }
                BlockPos support = new BlockPos(x, -1, z);
                structural.add(support);
                active.add(new RitualStructureRules.PatternSlot(support, RitualStructureRules.Role.STRUCTURAL));
            }
        }

        RUNE_OFFSETS = List.copyOf(runes);
        STRUCTURAL_OFFSETS = List.copyOf(structural);
        ACTIVE_PATTERN = List.copyOf(active);

        List<RitualStructureRules.PatternSlot> detection = new ArrayList<>();
        detection.add(new RitualStructureRules.PatternSlot(BlockPos.ZERO, RitualStructureRules.Role.CENTER));
        detection.addAll(ACTIVE_PATTERN);
        DETECTION_PATTERN = List.copyOf(detection);
    }

    private SummoningRitualStructure() {}

    public static List<BlockPos> runeOffsets() {
        return RUNE_OFFSETS;
    }

    public static List<BlockPos> structuralOffsets() {
        return STRUCTURAL_OFFSETS;
    }

    public static boolean detect(ServerLevel level, BlockPos center) {
        return RitualStructureRules.detectPattern(level, center, DETECTION_PATTERN).valid();
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center) {
        return RitualStructureRules.detectPattern(level, center, ACTIVE_PATTERN).valid();
    }

    public static boolean hasRuneSquare(ServerLevel level, BlockPos center) {
        List<RitualStructureRules.PatternSlot> runePattern = RUNE_OFFSETS.stream()
                .map(offset -> new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.RUNE))
                .toList();
        return RitualStructureRules.countPatternMatches(level, center, runePattern) >= 5;
    }
}