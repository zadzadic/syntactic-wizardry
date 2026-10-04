package com.proxpero.syntacticwizardry;

import com.proxpero.syntacticwizardry.client.ProtectionClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ProtectionNetwork {
    private static final IPayloadHandler<ProtectionSyncPayload> SYNC_HANDLER = new SyncHandler();
    private static final IPayloadHandler<ProtectionPreparedAreaPayload> AREA_HANDLER = new AreaHandler();

    private ProtectionNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ProtectionSyncPayload.TYPE, ProtectionSyncPayload.STREAM_CODEC, SYNC_HANDLER);
        registrar.playToServer(ProtectionPreparedAreaPayload.TYPE, ProtectionPreparedAreaPayload.STREAM_CODEC, AREA_HANDLER);
    }

    private static final class SyncHandler implements IPayloadHandler<ProtectionSyncPayload> {
        @Override
        public void handle(ProtectionSyncPayload payload, IPayloadContext context) {
            ProtectionClientState.apply(payload);
        }
    }

    private static final class AreaHandler implements IPayloadHandler<ProtectionPreparedAreaPayload> {
        @Override
        public void handle(ProtectionPreparedAreaPayload payload, IPayloadContext context) {
            if (!(context.player() instanceof ServerPlayer player)) return;
            BlockPos center = BlockPos.of(payload.center());
            ProtectionPreparedAreaData data = ProtectionPreparedAreaData.get(player.serverLevel());
            if (!payload.hasArea()) {
                data.removeOwner(player.getUUID());
                return;
            }

            data.put(new ProtectionPreparedAreaData.Config(
                    player.getUUID(), center,
                    ProtectionAreaShape.fromOrdinal(payload.shape()),
                    payload.minX(), payload.minY(), payload.minZ(),
                    payload.maxX(), payload.maxY(), payload.maxZ()));
        }
    }
}