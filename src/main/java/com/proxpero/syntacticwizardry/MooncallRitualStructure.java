package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;\nimport net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class MooncallRitualStructure {
    public record Detection(boolean valid, String error, MooncallPhase phase) {}

    private static final List<RitualStructureRules.PatternSlot> RUNE_PATTERN = buildRunePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(RUNE_PATTERN);

    private MooncallRitualStructure() {}

    public static BlockPos obsidianOffset(MooncallPhase phase) {
        MooncallPhase actual = phase == null ? MooncallPhase.FULL_MOON : phase;
        return switch (actual) {
            case FULL_MOON -> new BlockPos(0, 0, -2);
            case WANING_GIBBOUS -> new BlockPos(2, 0, -2);
            case LAST_QUARTER -> new BlockPos(2, 0, 0);
            case WANING_CRESCENT -> new BlockPos(2, 0, 2);
            case NEW_MOON -> new BlockPos(0, 0, 2);
            case WAXING_CRESCENT -> new BlockPos(-2, 0, 2);
            case FIRST_QUARTER -> new BlockPos(-2, 0, 0);
            case WAXING_GIBBOUS -> new BlockPos(-2, 0, -2);
        };
    }

    public static Detection detect(ServerLevel level, BlockPos center) {
        RitualStructureRules.PatternMatch common = RitualStructureRules.detectPattern(level, center, DETECTION_PATTERN);
        if (!common.valid()) {
            if (common.failedRole() == RitualStructureRules.Role.CENTER) {
                return invalid("The Center must be a Mature Crystal.");
            }
            return invalid("The Mooncall rune circle is incomplete.");
        }

        MooncallPhase found = null;
        for (MooncallPhase phase : MooncallPhase.values()) {
            RitualStructureRules.PatternSlot marker = RitualStructureRules.PatternSlot.specific(
                    obsidianOffset(phase), Blocks.OBSIDIAN);
            if (!RitualStructureRules.detectPattern(level, center, List.of(marker)).valid()) continue;
            if (found != null) {
                return invalid("Mooncall requires exactly one Obsidian phase marker.");
            }
            found = phase;
        }

        if (found == null) {
            return invalid("Mooncall requires one Obsidian phase marker two blocks from the Center.");
        }

        return new Detection(true, "", found);
    }

    public static boolean hasPhaseMarker(ServerLevel level, BlockPos center) {
        for (MooncallPhase phase : MooncallPhase.values()) {
            RitualStructureRules.PatternSlot marker = RitualStructureRules.PatternSlot.specific(
                    obsidianOffset(phase), Blocks.OBSIDIAN);
            if (RitualStructureRules.detectPattern(level, center, List.of(marker)).valid()) return true;
        }
        return false;
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, MooncallPhase phase) {
        if (phase == null) return false;
        if (!RitualStructureRules.detectPattern(level, center, RUNE_PATTERN).valid()) return false;
        return RitualStructureRules.detectPattern(
                level,
                center,
                List.of(RitualStructureRules.PatternSlot.specific(obsidianOffset(phase), Blocks.OBSIDIAN))).valid();
    }

    private static List<RitualStructureRules.PatternSlot> buildRunePattern() {
        return EclipseRitualStructure.runeOffsets().stream()
                .map(offset -> new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.RUNE))
                .toList();
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