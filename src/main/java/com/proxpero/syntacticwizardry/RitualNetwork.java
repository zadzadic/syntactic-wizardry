package com.proxpero.syntacticwizardry;

import com.proxpero.syntacticwizardry.client.EclipseClientState;
import com.proxpero.syntacticwizardry.client.MooncallClientState;
import com.proxpero.syntacticwizardry.client.SummoningClientState;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class RitualNetwork {
    private static final IPayloadHandler<EclipseSyncPayload> ECLIPSE_SYNC_HANDLER = new EclipseSyncHandler();
    private static final IPayloadHandler<MooncallSyncPayload> MOONCALL_SYNC_HANDLER = new MooncallSyncHandler();
    private static final IPayloadHandler<SummoningSyncPayload> SUMMONING_SYNC_HANDLER = new SummoningSyncHandler();
    private static final IPayloadHandler<RitualControlPayload> CONTROL_HANDLER = new RitualControlHandler();

    private RitualNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EclipseSyncPayload.TYPE, EclipseSyncPayload.STREAM_CODEC, ECLIPSE_SYNC_HANDLER);
        registrar.playToClient(MooncallSyncPayload.TYPE, MooncallSyncPayload.STREAM_CODEC, MOONCALL_SYNC_HANDLER);
        registrar.playToClient(SummoningSyncPayload.TYPE, SummoningSyncPayload.STREAM_CODEC, SUMMONING_SYNC_HANDLER);
        registrar.playToServer(RitualControlPayload.TYPE, RitualControlPayload.STREAM_CODEC, CONTROL_HANDLER);
    }

    private static final class EclipseSyncHandler implements IPayloadHandler<EclipseSyncPayload> {
        @Override
        public void handle(EclipseSyncPayload payload, IPayloadContext context) {
            EclipseClientState.apply(payload);
        }
    }

    private static final class MooncallSyncHandler implements IPayloadHandler<MooncallSyncPayload> {
        @Override
        public void handle(MooncallSyncPayload payload, IPayloadContext context) {
            MooncallClientState.apply(payload);
        }
    }

    private static final class SummoningSyncHandler implements IPayloadHandler<SummoningSyncPayload> {
        @Override
        public void handle(SummoningSyncPayload payload, IPayloadContext context) {
            SummoningClientState.apply(payload);
        }
    }

    private static final class RitualControlHandler implements IPayloadHandler<RitualControlPayload> {
        @Override
        public void handle(RitualControlPayload payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                EclipseRitualEvents.handleControl(player, payload);
                MooncallRitualEvents.handleControl(player, payload);
                SummoningRitualEvents.handleControl(player, payload);
            }
        }
    }
}
