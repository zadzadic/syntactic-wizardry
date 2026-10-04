package com.proxpero.syntacticwizardry;

import com.proxpero.syntacticwizardry.client.PermanencyClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class PermanencyNetwork {
    private static final IPayloadHandler<PermanencySyncPayload> SYNC_HANDLER = new SyncHandler();
    private static final IPayloadHandler<PermanencyPreparedConfigPayload> CONFIG_HANDLER = new ConfigHandler();

    private PermanencyNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(PermanencySyncPayload.TYPE, PermanencySyncPayload.STREAM_CODEC, SYNC_HANDLER);
        registrar.playToServer(
                PermanencyPreparedConfigPayload.TYPE,
                PermanencyPreparedConfigPayload.STREAM_CODEC,
                CONFIG_HANDLER);
    }

    private static final class SyncHandler implements IPayloadHandler<PermanencySyncPayload> {
        @Override
        public void handle(PermanencySyncPayload payload, IPayloadContext context) {
            PermanencyClientState.apply(payload);
        }
    }

    private static final class ConfigHandler implements IPayloadHandler<PermanencyPreparedConfigPayload> {
        @Override
        public void handle(PermanencyPreparedConfigPayload payload, IPayloadContext context) {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PermanencyPreparedConfigData data =
                    PermanencyPreparedConfigData.get(player.serverLevel());

            if (!payload.hasConfig()) {
                data.removeOwner(player.getUUID());
                return;
            }

            BlockPos center = BlockPos.of(payload.center());
            PermanencyPreparedConfigData.Config config =
                    new PermanencyPreparedConfigData.Config(
                            player.getUUID(),
                            center,
                            ProtectionAreaShape.fromOrdinal(payload.shape()),
                            payload.minX(), payload.minY(), payload.minZ(),
                            payload.maxX(), payload.maxY(), payload.maxZ(),
                            payload.yaw(), payload.pitch(),
                            payload.plan(), payload.settings());

            if (!PermanencyPreparedConfigData.hasEffect(config.plan())) {
                data.removeOwner(player.getUUID());
                return;
            }

            data.put(config);
        }
    }
}
