package com.proxpero.syntacticwizardry;

import com.proxpero.syntacticwizardry.client.EclipseClientState;
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
    private static final IPayloadHandler<RitualControlPayload> CONTROL_HANDLER = new RitualControlHandler();

    private RitualNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EclipseSyncPayload.TYPE, EclipseSyncPayload.STREAM_CODEC, ECLIPSE_SYNC_HANDLER);
        registrar.playToServer(RitualControlPayload.TYPE, RitualControlPayload.STREAM_CODEC, CONTROL_HANDLER);
    }

    private static final class EclipseSyncHandler implements IPayloadHandler<EclipseSyncPayload> {
        @Override
        public void handle(EclipseSyncPayload payload, IPayloadContext context) {
            EclipseClientState.apply(payload);
        }
    }

    private static final class RitualControlHandler implements IPayloadHandler<RitualControlPayload> {
        @Override
        public void handle(RitualControlPayload payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) EclipseRitualEvents.handleControl(player, payload);
        }
    }
}
