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

    private EclipseRitualStructure() {}

    public static List<BlockPos> runeOffsets() {
        return RUNE_OFFSETS;
    }

    public static Detection detect(ServerLevel level, BlockPos center) {
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) {
            return invalid("The Center must be a Mature Crystal.");
        }
        if (!level.getBlockState(center.below()).is(Blocks.OBSIDIAN)) {
            return invalid("Eclipse requires Obsidian directly beneath the Mature Crystal.");
        }

        for (BlockPos offset : RUNE_OFFSETS) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) {
                return invalid("The Eclipse rune circle is incomplete.");
            }
        }

        List<FocusRef> foci = scanFoci(level, center);
        if (foci.isEmpty()) return invalid("Eclipse requires at least one Focus Block.");

        int potence = suppliedPotence(foci);
        RitualStructureRules.FocusMaterial required = RitualStructureRules.requiredTier(potence);
        RitualStructureRules.FocusMaterial highest = highestTier(foci);
        if (highest == null || highest.ordinal() < required.ordinal()) {
            return invalid("The Focus tier is too low for Potence " + potence + ".");
        }

        return new Detection(true, "", potence, List.copyOf(foci));
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, List<FocusRef> expectedFoci) {
        if (!level.getBlockState(center.below()).is(Blocks.OBSIDIAN)) return false;
        for (BlockPos offset : RUNE_OFFSETS) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) return false;
        }
        for (FocusRef focus : expectedFoci) {
            if (!focus.material().matches(level.getBlockState(focus.position()))) return false;
        }
        return true;
    }

    private static List<FocusRef> scanFoci(ServerLevel level, BlockPos center) {
        List<FocusRef> result = new ArrayList<>();
        for (BlockPos offset : RitualStructureRules.focusSlots()) {
            BlockPos pos = center.offset(offset);
            RitualStructureRules.FocusMaterial material = RitualStructureRules.focusMaterial(level.getBlockState(pos));
            if (material != null) result.add(new FocusRef(pos.immutable(), material));
        }
        return result;
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
