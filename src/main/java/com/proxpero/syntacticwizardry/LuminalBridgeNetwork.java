package com.proxpero.syntacticwizardry;

import com.proxpero.syntacticwizardry.client.LuminalBridgeClientState;
import com.proxpero.syntacticwizardry.client.LuminalBridgeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class LuminalBridgeNetwork {
    private LuminalBridgeNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                LuminalBridgeSyncPayload.TYPE,
                LuminalBridgeSyncPayload.STREAM_CODEC,
                new SyncHandler());
        registrar.playToClient(
                LuminalBridgeDestinationsPayload.TYPE,
                LuminalBridgeDestinationsPayload.STREAM_CODEC,
                new DestinationsHandler());
        registrar.playToServer(
                LuminalBridgeTravelPayload.TYPE,
                LuminalBridgeTravelPayload.STREAM_CODEC,
                new TravelHandler());
    }

    private static final class SyncHandler implements IPayloadHandler<LuminalBridgeSyncPayload> {
        @Override
        public void handle(LuminalBridgeSyncPayload payload, IPayloadContext context) {
            LuminalBridgeClientState.apply(payload);
        }
    }

    private static final class DestinationsHandler implements IPayloadHandler<LuminalBridgeDestinationsPayload> {
        @Override
        public void handle(LuminalBridgeDestinationsPayload payload, IPayloadContext context) {
            Minecraft.getInstance().setScreen(new LuminalBridgeScreen(payload));
        }
    }

    private static final class TravelHandler implements IPayloadHandler<LuminalBridgeTravelPayload> {
        @Override
        public void handle(LuminalBridgeTravelPayload payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                LuminalBridgeRitualEvents.travel(player, payload.sourceId(), payload.destinationId());
            }
        }
    }
}
