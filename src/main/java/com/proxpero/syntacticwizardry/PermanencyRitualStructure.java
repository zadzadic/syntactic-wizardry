package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical structure for Permanency.
 *
 * Rune glyph/color/facing are never validated. Nether Bricks in the reference
 * pattern are generic Structural slots. Gold and Diamond are exact Specific slots.
 */
public final class PermanencyRitualStructure {
    public record RunePlacement(BlockPos offset, int glyph) {}
    public record Detection(boolean valid, String error) {}

    public static final BlockPos GOLD_OFFSET = new BlockPos(0, -1, 0);

    private static final List<BlockPos> DIAMOND_OFFSETS = List.of(
            new BlockPos(-2, 0, -2),
            new BlockPos(2, 0, -2),
            new BlockPos(-2, 0, 2),
            new BlockPos(2, 0, 2));

    private static final List<BlockPos> STRUCTURAL_OFFSETS = List.of(
            new BlockPos(-2, -1, -2),
            new BlockPos(2, -1, -2),
            new BlockPos(-2, -1, 2),
            new BlockPos(2, -1, 2),
            new BlockPos(-2, 1, -2),
            new BlockPos(2, 1, -2),
            new BlockPos(-2, 1, 2),
            new BlockPos(2, 1, 2));

    private static final List<RunePlacement> RUNES = List.of(
            new RunePlacement(new BlockPos(-1, -1, -3), 7),
            new RunePlacement(new BlockPos(0, -1, -3), 7),
            new RunePlacement(new BlockPos(1, -1, -3), 5),
            new RunePlacement(new BlockPos(-3, -1, -1), 2),
            new RunePlacement(new BlockPos(-1, -1, -1), 9),
            new RunePlacement(new BlockPos(1, -1, -1), 11),
            new RunePlacement(new BlockPos(3, -1, -1), 8),
            new RunePlacement(new BlockPos(-3, -1, 0), 1),
            new RunePlacement(new BlockPos(3, -1, 0), 15),
            new RunePlacement(new BlockPos(-3, -1, 1), 15),
            new RunePlacement(new BlockPos(-1, -1, 1), 10),
            new RunePlacement(new BlockPos(1, -1, 1), 5),
            new RunePlacement(new BlockPos(3, -1, 1), 0),
            new RunePlacement(new BlockPos(-1, -1, 3), 7),
            new RunePlacement(new BlockPos(0, -1, 3), 4),
            new RunePlacement(new BlockPos(1, -1, 3), 12));

    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN = buildActivePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(ACTIVE_PATTERN);

    private PermanencyRitualStructure() {}

    public static List<BlockPos> diamondOffsets() {
        return DIAMOND_OFFSETS;
    }

    public static List<BlockPos> structuralOffsets() {
        return STRUCTURAL_OFFSETS;
    }

    public static List<RunePlacement> runes() {
        return RUNES;
    }

    public static Detection detect(ServerLevel level, BlockPos center) {
        RitualStructureRules.PatternMatch match =
                RitualStructureRules.detectPattern(level, center, DETECTION_PATTERN);
        if (match.valid()) return new Detection(true, "");

        return switch (match.failedRole()) {
            case CENTER -> new Detection(false, "The Center must be a Mature Crystal.");
            case RUNE -> new Detection(false, "The Permanency rune pattern is incomplete.");
            case STRUCTURAL -> new Detection(false, "The Permanency structural pattern is incomplete.");
            case SPECIFIC -> new Detection(false, "Permanency requires the specific Gold and Diamond Blocks shown by the Armillary.");
            default -> new Detection(false, "The Permanency ritual pattern is incomplete or incorrect.");
        };
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center) {
        return RitualStructureRules.detectPattern(level, center, ACTIVE_PATTERN).valid();
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos center) {
        return RitualStructureRules.countPatternMatches(level, center, ACTIVE_PATTERN) >= 8;
    }

    private static List<RitualStructureRules.PatternSlot> buildActivePattern() {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(RitualStructureRules.PatternSlot.specific(GOLD_OFFSET, Blocks.GOLD_BLOCK));
        for (BlockPos offset : DIAMOND_OFFSETS) {
            slots.add(RitualStructureRules.PatternSlot.specific(offset, Blocks.DIAMOND_BLOCK));
        }
        for (BlockPos offset : STRUCTURAL_OFFSETS) {
            slots.add(new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.STRUCTURAL));
        }
        for (RunePlacement placement : RUNES) {
            slots.add(new RitualStructureRules.PatternSlot(placement.offset(), RitualStructureRules.Role.RUNE));
        }
        return List.copyOf(slots);
    }

    private static List<RitualStructureRules.PatternSlot> withCenter(
            List<RitualStructureRules.PatternSlot> active) {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(new RitualStructureRules.PatternSlot(BlockPos.ZERO, RitualStructureRules.Role.CENTER));
        slots.addAll(active);
        return List.copyOf(slots);
    }
}
