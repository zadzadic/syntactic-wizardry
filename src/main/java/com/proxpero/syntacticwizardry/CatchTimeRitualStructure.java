package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Catch Time structure.
 *
 * Common ritual roles are validated exclusively through RitualStructureRules.
 * Lapis and Gold are ritual-specific semantic blocks and are checked separately.
 */
public final class CatchTimeRitualStructure {
    /** Glyph is retained only as an Armillary preview suggestion. It is never validated. */
    public record RunePlacement(BlockPos offset, int glyph) {}
    public record Detection(boolean valid, String error, CatchTimeSetting setting) {}

    public static final BlockPos LAPIS_OFFSET = new BlockPos(0, -1, 0);

    /** Previewed as Stone Bricks, but any valid Structural Block is accepted. */
    private static final List<BlockPos> STRUCTURAL_OFFSETS = List.of(
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

    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN = buildActivePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(ACTIVE_PATTERN);

    private CatchTimeRitualStructure() {}

    public static List<BlockPos> stoneBrickOffsets() {
        return STRUCTURAL_OFFSETS;
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
        RitualStructureRules.PatternMatch common = RitualStructureRules.detectPattern(level, center, DETECTION_PATTERN);
        if (!common.valid()) {
            return invalid("The Catch Time ritual pattern is incomplete or incorrect.");
        }

        if (!level.getBlockState(center.offset(LAPIS_OFFSET)).is(Blocks.LAPIS_BLOCK)) {
            return invalid("Catch Time requires a Lapis Block directly below the Mature Crystal.");
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
        if (!RitualStructureRules.detectPattern(level, center, ACTIVE_PATTERN).valid()) return false;
        if (!level.getBlockState(center.offset(LAPIS_OFFSET)).is(Blocks.LAPIS_BLOCK)) return false;

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

    private static List<RitualStructureRules.PatternSlot> buildActivePattern() {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        for (BlockPos offset : STRUCTURAL_OFFSETS) {
            slots.add(new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.STRUCTURAL));
        }
        for (RunePlacement placement : RUNES) {
            slots.add(new RitualStructureRules.PatternSlot(placement.offset(), RitualStructureRules.Role.RUNE));
        }
        return List.copyOf(slots);
    }

    private static List<RitualStructureRules.PatternSlot> withCenter(List<RitualStructureRules.PatternSlot> active) {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(new RitualStructureRules.PatternSlot(BlockPos.ZERO, RitualStructureRules.Role.CENTER));
        slots.addAll(active);
        return List.copyOf(slots);
    }

    private static Detection invalid(String error) {
        return new Detection(false, error, null);
    }
}