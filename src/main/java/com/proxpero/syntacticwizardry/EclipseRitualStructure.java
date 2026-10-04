package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class EclipseRitualStructure {
    public record FocusRef(BlockPos position, RitualStructureRules.FocusMaterial material) {}
    public record Detection(boolean valid, String error, int potence, List<FocusRef> foci) {}

    private static final List<BlockPos> RUNE_OFFSETS = List.of(
            new BlockPos(4,0,0), new BlockPos(4,0,1),
            new BlockPos(3,0,3), new BlockPos(1,0,4),
            new BlockPos(0,0,4), new BlockPos(-1,0,4),
            new BlockPos(-3,0,3), new BlockPos(-4,0,1),
            new BlockPos(-4,0,0), new BlockPos(-4,0,-1),
            new BlockPos(-3,0,-3), new BlockPos(-1,0,-4),
            new BlockPos(0,0,-4), new BlockPos(1,0,-4),
            new BlockPos(3,0,-3), new BlockPos(4,0,-1));

    private static final List<RitualStructureRules.PatternSlot> ACTIVE_PATTERN = buildActivePattern();
    private static final List<RitualStructureRules.PatternSlot> DETECTION_PATTERN = withCenter(ACTIVE_PATTERN);

    private EclipseRitualStructure() {}

    public static List<BlockPos> runeOffsets() {
        return RUNE_OFFSETS;
    }

    public static Detection detect(ServerLevel level, BlockPos center) {
        RitualStructureRules.PatternMatch match = RitualStructureRules.detectPattern(
                level, center, DETECTION_PATTERN, RitualStructureRules.focusSlots(), 1);
        if (!match.valid()) {
            if (match.failedRole() == RitualStructureRules.Role.CENTER) {
                return invalid("The Center must be a Mature Crystal.");
            }
            if (match.failedRole() == RitualStructureRules.Role.FOCUS) {
                return invalid("Eclipse requires at least one Focus Block.");
            }
            if (match.failedRole() == RitualStructureRules.Role.RUNE) {
                return invalid("The Eclipse rune circle is incomplete.");
            }
            return invalid("The Eclipse ritual structure is incomplete.");
        }

        List<FocusRef> foci = match.foci().stream()
                .map(focus -> new FocusRef(focus.position(), focus.material()))
                .toList();

        int potence = suppliedPotence(foci);
        RitualStructureRules.FocusMaterial required = RitualStructureRules.requiredTier(potence);
        RitualStructureRules.FocusMaterial highest = highestTier(foci);
        if (highest == null || highest.ordinal() < required.ordinal()) {
            return invalid("The Focus tier is too low for Potence " + potence + ".");
        }

        return new Detection(true, "", potence, List.copyOf(foci));
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, List<FocusRef> expectedFoci) {
        if (!RitualStructureRules.detectPattern(level, center, ACTIVE_PATTERN).valid()) return false;
        for (FocusRef focus : expectedFoci) {
            if (!RitualStructureRules.isValidForRole(
                    level, focus.position(), RitualStructureRules.Role.FOCUS)) return false;
        }
        return true;
    }

    private static List<RitualStructureRules.PatternSlot> buildActivePattern() {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(RitualStructureRules.PatternSlot.specific(new BlockPos(0, -1, 0), Blocks.OBSIDIAN));
        for (BlockPos offset : RUNE_OFFSETS) {
            slots.add(new RitualStructureRules.PatternSlot(offset, RitualStructureRules.Role.RUNE));
        }
        return List.copyOf(slots);
    }

    private static List<RitualStructureRules.PatternSlot> withCenter(List<RitualStructureRules.PatternSlot> active) {
        List<RitualStructureRules.PatternSlot> slots = new ArrayList<>();
        slots.add(new RitualStructureRules.PatternSlot(BlockPos.ZERO, RitualStructureRules.Role.CENTER));
        slots.addAll(active);
        return List.copyOf(slots);
    }

    private static int suppliedPotence(List<FocusRef> foci) {
        int iron = 0, gold = 0, emerald = 0, diamond = 0;
        for (FocusRef focus : foci) {
            switch (focus.material()) {
                case IRON -> iron++;
                case GOLD -> gold++;
                case EMERALD -> emerald++;
                case DIAMOND -> diamond++;
            }
        }
        int value = Math.min(iron, 4);
        value += Math.min(gold * 2, 6);
        value += Math.min(emerald * 3, 8);
        value += diamond * 4;
        return Math.max(1, value);
    }

    private static RitualStructureRules.FocusMaterial highestTier(List<FocusRef> foci) {
        RitualStructureRules.FocusMaterial result = null;
        for (FocusRef focus : foci) {
            if (result == null || focus.material().ordinal() > result.ordinal()) result = focus.material();
        }
        return result;
    }

    private static Detection invalid(String error) {
        return new Detection(false, error, 0, List.of());
    }
}