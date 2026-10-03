package com.proxpero.syntacticwizardry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ChalkRuneBlock extends Block {
    public static final MapCodec<ChalkRuneBlock> CODEC = simpleCodec(ChalkRuneBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty GLYPH = IntegerProperty.create("glyph", 0, 15);
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    private static final VoxelShape UP_SHAPE = Block.box(3,0,3,13,1,13);
    private static final VoxelShape DOWN_SHAPE = Block.box(3,15,3,13,16,13);
    private static final VoxelShape NORTH_SHAPE = Block.box(3,3,15,13,13,16);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3,3,0,13,13,1);
    private static final VoxelShape WEST_SHAPE = Block.box(15,3,3,16,13,13);
    private static final VoxelShape EAST_SHAPE = Block.box(0,3,3,1,13,13);

    public ChalkRuneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(GLYPH, 0)
                .setValue(COLOR, DyeColor.WHITE));
    }

    @Override protected MapCodec<? extends Block> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GLYPH, COLOR);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing=state.getValue(FACING);
        return Block.canSupportCenter(level,pos.relative(facing.getOpposite()),facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if(direction==state.getValue(FACING).getOpposite() && !state.canSurvive(level,pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state,direction,neighborState,level,pos,neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(FACING)) {
            case UP -> UP_SHAPE;
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }
}
