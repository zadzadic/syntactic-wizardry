package com.proxpero.syntacticwizardry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GoldenOrbBlock extends Block {
    public static final MapCodec<GoldenOrbBlock> CODEC = simpleCodec(GoldenOrbBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(4.0D, 1.0D, 4.0D, 12.0D, 3.0D, 12.0D),
            Block.box(2.0D, 3.0D, 2.0D, 14.0D, 5.0D, 14.0D),
            Block.box(1.0D, 5.0D, 1.0D, 15.0D, 11.0D, 15.0D),
            Block.box(2.0D, 11.0D, 2.0D, 14.0D, 13.0D, 14.0D),
            Block.box(4.0D, 13.0D, 4.0D, 12.0D, 15.0D, 12.0D)
    );

    public GoldenOrbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isCreative()) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("The Golden Orb is Creative-only."), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, ignored) -> new GoldenOrbMenu(id, inventory),
                    Component.translatable("block.syntacticwizardry.golden_orb")
            );
            serverPlayer.openMenu(provider);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
