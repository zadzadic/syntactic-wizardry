package com.arcane.magic.network;

import com.arcane.magic.builder.BuilderInventoryPanel;
import com.arcane.magic.builder.BuilderServerCommands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "syntacticwizardry", bus = EventBusSubscriber.Bus.MOD)
public final class BuilderLegacyNetworkEvents {
    private static final IPayloadHandler<ManaCompassCommandPayload> COMMAND_HANDLER = new CommandHandler();
    private static final IPayloadHandler<ManaCompassStatePayload> STATE_HANDLER = new StateHandler();

    private BuilderLegacyNetworkEvents() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ManaCompassCommandPayload.TYPE, ManaCompassCommandPayload.STREAM_CODEC, COMMAND_HANDLER);
        registrar.playToClient(ManaCompassStatePayload.TYPE, ManaCompassStatePayload.STREAM_CODEC, STATE_HANDLER);
    }

    private static final class CommandHandler implements IPayloadHandler<ManaCompassCommandPayload> {
        @Override
        public void handle(ManaCompassCommandPayload payload, IPayloadContext context) {
            if ("WAND_CLEAR".equals(payload.command()) && context.player() instanceof ServerPlayer player) {
                var main = player.getMainHandItem();
                var off = player.getOffhandItem();
                if (main.is(com.proxpero.syntacticwizardry.SyntacticWizardry.WAND.get())) {
                    com.proxpero.syntacticwizardry.WandBindingService.clear(main);
                    player.getInventory().setChanged();
                    return;
                }
                if (off.is(com.proxpero.syntacticwizardry.SyntacticWizardry.WAND.get())) {
                    com.proxpero.syntacticwizardry.WandBindingService.clear(off);
                    player.getInventory().setChanged();
                    return;
                }
            }
            BuilderServerCommands.handle(context.player(), payload.command());
        }
    }

    private static final class StateHandler implements IPayloadHandler<ManaCompassStatePayload> {
        @Override
        public void handle(ManaCompassStatePayload payload, IPayloadContext context) {
            BuilderInventoryPanel.handleState(payload.data());
        }
    }
}
