package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ConduitSeatEvents {
    private ConduitSeatEvents() {}

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        int id = player.getPersistentData().getInt(ConduitBlock.SEAT_ID);
        if (id <= 0) return;

        ServerLevel level = player.serverLevel();
        Entity seat = level.getEntity(id);
        if (seat == null) {
            player.getPersistentData().remove(ConduitBlock.SEAT_ID);
            return;
        }

        if (player.getVehicle() != seat
                || !seat.getPersistentData().getBoolean(ConduitBlock.SEAT_TAG)) {
            cleanup(player, seat);
            return;
        }

        BlockPos pos = new BlockPos(
                seat.getPersistentData().getInt(ConduitBlock.X_TAG),
                seat.getPersistentData().getInt(ConduitBlock.Y_TAG),
                seat.getPersistentData().getInt(ConduitBlock.Z_TAG));

        if (ConduitRegistry.block() == null || !level.getBlockState(pos).is(ConduitRegistry.block())) {
            cleanup(player, seat);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int id = player.getPersistentData().getInt(ConduitBlock.SEAT_ID);
        if (id > 0) {
            Entity seat = player.serverLevel().getEntity(id);
            if (seat != null) seat.discard();
        }
        player.getPersistentData().remove(ConduitBlock.SEAT_ID);
    }

    private static void cleanup(ServerPlayer player, Entity seat) {
        player.stopRiding();
        seat.discard();
        player.getPersistentData().remove(ConduitBlock.SEAT_ID);
    }
}
