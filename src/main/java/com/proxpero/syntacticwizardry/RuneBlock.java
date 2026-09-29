package com.proxpero.syntacticwizardry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Transparent, non-colliding one-block Rune trigger. The block cell itself is the trigger volume. */
public final class RuneBlock extends Block implements EntityBlock {
    public static final MapCodec<RuneBlock> CODEC = simpleCodec(RuneBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty GLYPH = IntegerProperty.create("glyph", 0, 5);

    public RuneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(GLYPH, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GLYPH);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isRemoved() || entity.isSpectator()) return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RuneBlockEntity rune) rune.trigger(entity);
    }
}
