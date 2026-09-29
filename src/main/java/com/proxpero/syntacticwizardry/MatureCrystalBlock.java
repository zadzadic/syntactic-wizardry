package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MatureCrystalBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(5.5D, 0.0D, 5.5D, 10.5D, 12.0D, 10.5D);
    public MatureCrystalBlock(Properties properties) { super(properties); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
}
