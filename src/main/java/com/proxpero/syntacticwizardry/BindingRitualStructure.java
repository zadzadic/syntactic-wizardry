package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public final class BindingRitualStructure {
    /** Glyph is retained only as an Armillary preview suggestion. It is never validated. */
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

    /** Previewed as Magic Light, but any valid Structural Block is accepted here. */
    private static final List<BlockPos> LIGHTS = List.of(
            new BlockPos(-1, 0, -2),
            new BlockPos(1, 0, -2),
            new BlockPos(-2, 0, -1),
            new BlockPos(2, 0, -1),
            new BlockPos(-2, 0, 1),
            new BlockPos(2, 0, 1),
            new BlockPos(-1, 0, 2),
            new BlockPos(1, 0, 2));

    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN = buildActivePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(ACTIVE_PATTERN);

    private BindingRitualStructure() {}

    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static List<BlockPos> lightOffsets() {
        return LIGHTS;
    }

    public static boolean detect(ServerLevel level, BlockPos center) {
        return RitualStructureRules.detectPattern(level, center, DETECTION_PATTERN).valid();
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center) {
        return RitualStructureRules.detectPattern(level, center, ACTIVE_PATTERN).valid();
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos center) {
        return RitualStructureRules.countPatternMatches(level, center, ACTIVE_PATTERN) >= 6;
    }

    private static List<RitualStructureRules.PatternSlot> buildActivePattern() {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        for (RunePlacement placement : RUNES) {
            slots.add(new RitualStructureRules.PatternSlot(placement.offset(), RitualStructureRules.Role.RUNE));
        }
        for (BlockPos offset : LIGHTS) {
            slots.add(new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.STRUCTURAL));
        }
        return List.copyOf(slots);
    }

    private static List<RitualStructureRules.PatternSlot> withCenter(List<RitualStructureRules.PatternSlot> active) {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(new RitualStructureRules.PatternSlot(BlockPos.ZERO, RitualStructureRules.Role.CENTER));
        slots.addAll(active);
        return List.copyOf(slots);
    }
}