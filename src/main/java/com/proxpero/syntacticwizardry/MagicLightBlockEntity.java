package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public final class MagicLightBlockEntity extends BlockEntity {
    public static final BlockEntityTicker<MagicLightBlockEntity> CLIENT_TICKER = MagicLightBlockEntity::clientTick;

    public MagicLightBlockEntity(BlockPos pos, BlockState state) {
        super(MagicLightRegistry.blockEntityType(), pos, state);
    }

    private static void clientTick(Level level, BlockPos pos, BlockState state, MagicLightBlockEntity blockEntity) {
        // Billboard sprite renderer handles the visual now.
    }
}
