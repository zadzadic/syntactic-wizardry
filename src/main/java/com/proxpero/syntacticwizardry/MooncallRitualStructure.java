package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class MooncallRitualStructure {
    public record Detection(boolean valid, String error, MooncallPhase phase) {}

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
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) {
            return invalid("The Center must be a Mature Crystal.");
        }

        for (BlockPos offset : EclipseRitualStructure.runeOffsets()) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) {
                return invalid("The Mooncall rune circle is incomplete.");
            }
        }

        MooncallPhase found = null;
        for (MooncallPhase phase : MooncallPhase.values()) {
            if (!level.getBlockState(center.offset(obsidianOffset(phase))).is(Blocks.OBSIDIAN)) continue;
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
            if (level.getBlockState(center.offset(obsidianOffset(phase))).is(Blocks.OBSIDIAN)) return true;
        }
        return false;
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, MooncallPhase phase) {
        if (phase == null) return false;
        if (!level.getBlockState(center.offset(obsidianOffset(phase))).is(Blocks.OBSIDIAN)) return false;

        for (BlockPos offset : EclipseRitualStructure.runeOffsets()) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(center.offset(offset)))) return false;
        }

        return true;
    }

    private static Detection invalid(String error) {
        return new Detection(false, error, null);
    }
}
