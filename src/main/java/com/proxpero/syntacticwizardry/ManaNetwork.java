package com.proxpero.syntacticwizardry;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ManaNetwork {
    private static final IPayloadHandler<ManaSyncPayload> MANA_SYNC_HANDLER = new ManaSyncHandler();
    private static final IPayloadHandler<FlightSyncPayload> FLIGHT_SYNC_HANDLER = new FlightSyncHandler();

    private ManaNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(ManaSyncPayload.TYPE, ManaSyncPayload.STREAM_CODEC, MANA_SYNC_HANDLER);
        registrar.playToClient(FlightSyncPayload.TYPE, FlightSyncPayload.STREAM_CODEC, FLIGHT_SYNC_HANDLER);
    }

    private static final class ManaSyncHandler implements IPayloadHandler<ManaSyncPayload> {
        @Override
        public void handle(ManaSyncPayload payload, IPayloadContext context) {
            ManaService.setClientMana(context.player(), payload.mana());
        }
    }

    private static final class FlightSyncHandler implements IPayloadHandler<FlightSyncPayload> {
        @Override
        public void handle(FlightSyncPayload payload, IPayloadContext context) {
            FlightService.setClientPotence(context.player(), payload.potence());
        }
    }
}
