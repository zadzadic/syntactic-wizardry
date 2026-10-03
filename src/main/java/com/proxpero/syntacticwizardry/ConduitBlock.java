package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ConduitBlock extends Block {
    static final String SEAT_TAG = "syntactic_conduit_seat";
    static final String SEAT_ID = "syntactic_conduit_seat_id";
    static final String X_TAG = "syntactic_conduit_x";
    static final String Y_TAG = "syntactic_conduit_y";
    static final String Z_TAG = "syntactic_conduit_z";

    public ConduitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(false);
        }
        if (serverPlayer.getVehicle() != null) return InteractionResult.sidedSuccess(false);

        ArmorStand seat = new ArmorStand(
                level,
                pos.getX() + 0.5D,
                pos.getY() - 1.55D,
                pos.getZ() + 0.5D);
        seat.setInvisible(true);
        seat.setNoGravity(true);
        seat.setInvulnerable(true);
        seat.getPersistentData().putBoolean(SEAT_TAG, true);
        seat.getPersistentData().putInt(X_TAG, pos.getX());
        seat.getPersistentData().putInt(Y_TAG, pos.getY());
        seat.getPersistentData().putInt(Z_TAG, pos.getZ());

        server.addFreshEntity(seat);
        if (!serverPlayer.startRiding(seat, true)) {
            seat.discard();
            return InteractionResult.sidedSuccess(false);
        }

        serverPlayer.getPersistentData().putInt(SEAT_ID, seat.getId());
        return InteractionResult.sidedSuccess(false);
    }
}
