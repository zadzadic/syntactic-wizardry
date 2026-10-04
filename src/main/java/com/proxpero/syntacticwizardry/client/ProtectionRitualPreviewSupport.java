package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ChalkRegistry;
import com.proxpero.syntacticwizardry.ChalkRuneBlock;
import com.proxpero.syntacticwizardry.ProtectionRitualStructure;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.RitualStructureRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ProtectionRitualPreviewSupport {
    private ProtectionRitualPreviewSupport() {}

    public static void append(RitualDefinition ritual, BlockPos center,
                              List<PreparedRitualPreview.GhostBlock> blocks) {
        if (ritual != RitualDefinition.PROTECTION || center == null || blocks == null) return;

        blocks.removeIf(block -> block.role() == RitualStructureRules.Role.FOCUS);

        blocks.add(new PreparedRitualPreview.GhostBlock(
                center.offset(ProtectionRitualStructure.centerIronOffset()),
                RitualStructureRules.Role.STRUCTURAL, null, "Iron Block",
                Blocks.IRON_BLOCK.defaultBlockState()));

        for (BlockPos offset : ProtectionRitualStructure.foundationIronOffsets()) {
            blocks.add(new PreparedRitualPreview.GhostBlock(
                    center.offset(offset), RitualStructureRules.Role.STRUCTURAL, null,
                    "Iron Block", Blocks.IRON_BLOCK.defaultBlockState()));
        }

        if (ChalkRegistry.block() != null) {
            for (ProtectionRitualStructure.RunePlacement placement : ProtectionRitualStructure.runes()) {
                BlockState state = ChalkRegistry.block().defaultBlockState();
                state = (BlockState) state.setValue(ChalkRuneBlock.FACING, Direction.UP);
                state = (BlockState) state.setValue(ChalkRuneBlock.GLYPH, placement.glyph());
                state = (BlockState) state.setValue(ChalkRuneBlock.COLOR, DyeColor.BLACK);
                blocks.add(new PreparedRitualPreview.GhostBlock(
                        center.offset(placement.offset()), RitualStructureRules.Role.RUNE, null,
                        "Black Chalk Rune " + placement.glyph(), state));
            }
        }
    }
}