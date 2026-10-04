package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/** Physical pattern for the Protection ritual, relative to the Mature Crystal. */
public final class ProtectionRitualStructure {
    /** Glyph is retained only as an Armillary preview suggestion. It is never validated. */
    public record RunePlacement(BlockPos offset, int glyph) {}

    /** Previewed as Iron, but any valid Structural Block is accepted. */
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

    /** Previewed as Iron, but any valid Structural Block is accepted. */
    private static final BlockPos CENTER_IRON = new BlockPos(0, -1, 0);
    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN = buildActivePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(ACTIVE_PATTERN);

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
        return RitualStructureRules.detectPattern(level, crystalCenter, DETECTION_PATTERN).valid();
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos crystalCenter) {
        return RitualStructureRules.detectPattern(level, crystalCenter, ACTIVE_PATTERN).valid();
    }

    public static boolean hasPatternHint(ServerLevel level, BlockPos crystalCenter) {
        return RitualStructureRules.countPatternMatches(level, crystalCenter, ACTIVE_PATTERN) >= 6;
    }

    private static List<RitualStructureRules.PatternSlot> buildActivePattern() {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(RitualStructureRules.PatternSlot.specific(CENTER_IRON, Blocks.IRON_BLOCK));
        for (BlockPos offset : FOUNDATION_IRON) {
            slots.add(RitualStructureRules.PatternSlot.specific(offset, Blocks.IRON_BLOCK));
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
}