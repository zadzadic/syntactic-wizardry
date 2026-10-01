package com.proxpero.syntacticwizardry;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ManaNetwork {
    private static final IPayloadHandler<ManaSyncPayload> MANA_SYNC_HANDLER = new ManaSyncHandler();
    private static final IPayloadHandler<FlightSyncPayload> FLIGHT_SYNC_HANDLER = new FlightSyncHandler();
    private static final IPayloadHandler<HighManaCompassStatePayload> HIGH_MANA_COMPASS_STATE_HANDLER = new HighManaCompassStateHandler();
    private static final IPayloadHandler<HighManaCompassMenuRequestPayload> HIGH_MANA_COMPASS_MENU_HANDLER = new HighManaCompassMenuHandler();
    private static final IPayloadHandler<FocusSlotCyclePayload> FOCUS_SLOT_CYCLE_HANDLER = new FocusSlotCycleHandler();
    private static final IPayloadHandler<WandClearBindingPayload> WAND_CLEAR_BINDING_HANDLER = new WandClearBindingHandler();

    private ManaNetwork() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ManaSyncPayload.TYPE, ManaSyncPayload.STREAM_CODEC, MANA_SYNC_HANDLER);
        registrar.playToClient(FlightSyncPayload.TYPE, FlightSyncPayload.STREAM_CODEC, FLIGHT_SYNC_HANDLER);
        registrar.playToClient(HighManaCompassStatePayload.TYPE, HighManaCompassStatePayload.STREAM_CODEC, HIGH_MANA_COMPASS_STATE_HANDLER);
        registrar.playToServer(HighManaCompassMenuRequestPayload.TYPE, HighManaCompassMenuRequestPayload.STREAM_CODEC, HIGH_MANA_COMPASS_MENU_HANDLER);
        registrar.playToServer(FocusSlotCyclePayload.TYPE, FocusSlotCyclePayload.STREAM_CODEC, FOCUS_SLOT_CYCLE_HANDLER);
        registrar.playToServer(WandClearBindingPayload.TYPE, WandClearBindingPayload.STREAM_CODEC, WAND_CLEAR_BINDING_HANDLER);
    }

    private static final class ManaSyncHandler implements IPayloadHandler<ManaSyncPayload> {
        @Override
        public void handle(ManaSyncPayload payload, IPayloadContext context) {
            ManaService.setClientState(context.player(), payload.mana(), payload.castingExperience(), payload.maxManaBonus());
        }
    }

    private static final class HighManaCompassStateHandler implements IPayloadHandler<HighManaCompassStatePayload> {
        @Override public void handle(HighManaCompassStatePayload payload, IPayloadContext context) { HighManaCompassClientState.set(payload); }
    }

    private static final class HighManaCompassMenuHandler implements IPayloadHandler<HighManaCompassMenuRequestPayload> {
        @Override public void handle(HighManaCompassMenuRequestPayload payload, IPayloadContext context) {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player && HighManaCompassService.holdsCompass(player)) HighManaCompassService.openMenu(player);
        }
    }

    private static final class FocusSlotCycleHandler implements IPayloadHandler<FocusSlotCyclePayload> {
        @Override public void handle(FocusSlotCyclePayload payload, IPayloadContext context) {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) return;
            int delta = payload.delta();

            if (Math.abs(delta) >= 10) {
                net.minecraft.world.item.ItemStack runecaster = heldRunecaster(player);
                if (!runecaster.isEmpty()) {
                    if (Math.abs(delta) >= 100) {
                        int selected = RunecasterRuneStorage.getSelectedSlot(runecaster);
                        int rotation = RunecasterRuneStorage.rotateSelected(runecaster, delta);
                        int activeType = RunecasterRuneStorage.activeType(runecaster, selected);
                        SpellComponentDefinition definition = SpellComponents.byType(activeType);
                        String activeName = definition == null ? "Empty" : definition.displayName();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "Runestone " + (selected + 1) + ": " + activeName + " [" + (rotation + 1) + "]"), true);
                    } else {
                        int selected = RunecasterRuneStorage.cycleSelectedSlot(runecaster, delta);
                        net.minecraft.world.item.ItemStack rune = RunecasterRuneStorage.getMountedRune(runecaster, selected);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                                "Selected Runestone " + (selected + 1) + (rune.isEmpty() ? " (empty)" : "")), true);
                    }
                    player.getInventory().setChanged();
                    player.containerMenu.broadcastChanges();
                    return;
                }
            }

            FocusSpellStorage.cycleHeldFocus(player, delta);
        }

        private static net.minecraft.world.item.ItemStack heldRunecaster(net.minecraft.world.entity.player.Player player) {
            net.minecraft.world.item.ItemStack main = player.getMainHandItem();
            if (main.getItem() instanceof RunecasterItem) return main;
            net.minecraft.world.item.ItemStack off = player.getOffhandItem();
            if (off.getItem() instanceof RunecasterItem) return off;
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
    }

    private static final class WandClearBindingHandler implements IPayloadHandler<WandClearBindingPayload> {
        @Override public void handle(WandClearBindingPayload payload, IPayloadContext context) {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) return;
            net.minecraft.world.item.ItemStack stack = payload.mainHand() ? player.getMainHandItem() : player.getOffhandItem();
            if (!stack.is(SyntacticWizardry.WAND.get())) return;
            boolean cleared = WandBindingService.clear(stack);
            player.getInventory().setChanged();
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(cleared ? "Wand binding cleared." : "Wand has no binding."), true);
        }
    }

    private static final class FlightSyncHandler implements IPayloadHandler<FlightSyncPayload> {
        @Override
        public void handle(FlightSyncPayload payload, IPayloadContext context) {
            FlightService.setClientPotence(context.player(), payload.potence());
        }
    }
}
