package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PylonSupport {
    private PylonSupport() {}
    public static BlockPos middleFromPart(BlockPos pos, BlockState state) {
        if (state.is(SyntacticWizardry.PYLON_MIDDLE.get())) return pos;
        if (state.is(SyntacticWizardry.PYLON_LOWER.get())) return pos.above();
        if (state.is(SyntacticWizardry.PYLON_UPPER.get())) return pos.below();
        return null;
    }
    public static boolean isPylonPart(BlockState state) {
        return state.is(SyntacticWizardry.PYLON_LOWER.get()) || state.is(SyntacticWizardry.PYLON_MIDDLE.get()) || state.is(SyntacticWizardry.PYLON_UPPER.get());
    }
    public static boolean isValid(ServerLevel level, BlockPos middle) {
        return level.getBlockState(middle).is(SyntacticWizardry.PYLON_MIDDLE.get())
                && level.getBlockState(middle.below()).is(SyntacticWizardry.PYLON_LOWER.get())
                && level.getBlockState(middle.above()).is(SyntacticWizardry.PYLON_UPPER.get());
    }
    public static boolean isRawStructure(Level level, BlockPos crystal) {
        return level.getBlockState(crystal).is(SyntacticWizardry.MATURE_CRYSTAL.get())
                && level.getBlockState(crystal.below()).is(Blocks.NETHER_BRICKS)
                && level.getBlockState(crystal.above()).is(Blocks.NETHER_BRICKS);
    }
    public static void form(ServerLevel level, BlockPos crystal) {
        level.setBlock(crystal.below(), SyntacticWizardry.PYLON_LOWER.get().defaultBlockState(), 3);
        level.setBlock(crystal, SyntacticWizardry.PYLON_MIDDLE.get().defaultBlockState(), 3);
        level.setBlock(crystal.above(), SyntacticWizardry.PYLON_UPPER.get().defaultBlockState(), 3);
    }
    public static void clear(Level level, BlockPos middle) {
        level.setBlock(middle.below(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(middle, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(middle.above(), Blocks.AIR.defaultBlockState(), 3);
    }
}
