package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BindingRitualEvents {
    private BindingRitualEvents() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos center = event.getPos();
        ItemStack held = event.getItemStack();

        if (!held.is(SyntacticWizardry.WAND.get())) return;
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return;
        if (!(level instanceof ServerLevel server)) return;
        if (!BindingRitualStructure.hasPatternHint(server, center)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (WandBindingService.get(held) != null) {
            player.displayClientMessage(
                    Component.literal("The Wand must be empty to activate a Ritual."),
                    true);
            return;
        }

        if (!BindingRitualStructure.detect(server, center)) {
            player.displayClientMessage(
                    Component.literal("The Binding Ritual pattern is incomplete or incorrect."),
                    true);
            return;
        }

        BindingRitualData.Entry entry = BindingRitualData.get(server).activate(
                server,
                center,
                player.getUUID());

        server.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
        syncLevel(server);

        String state = entry.powered() ? "activating" : "dormant: no Mana supply";
        player.displayClientMessage(
                Component.literal("Binding Ritual activated (" + state + ")."),
                true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BindingRitualData data = BindingRitualData.get(level);
        if (data.tick(level)) syncLevel(level);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncPlayer(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncPlayer(player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncPlayer(player);
    }

    public static void handleControl(ServerPlayer player, RitualControlPayload payload) {
        if (player == null || payload == null) return;

        ServerLevel level = player.serverLevel();
        BindingRitualData data = BindingRitualData.get(level);

        boolean changed = switch (payload.action()) {
            case "pause" -> data.setPaused(payload.id(), true);
            case "resume" -> data.setPaused(payload.id(), false);
            case "stop" -> data.stop(payload.id());
            case "rename" -> data.rename(payload.id(), payload.value());
            default -> false;
        };

        if (changed) syncLevel(level);
    }

    public static void syncPlayer(ServerPlayer player) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, payload(player.serverLevel()));
    }

    public static void syncLevel(ServerLevel level) {
        BindingSyncPayload payload = payload(level);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static BindingSyncPayload payload(ServerLevel level) {
        List<BindingSyncPayload.Entry> entries = new ArrayList<>();

        for (BindingRitualData.Entry entry : BindingRitualData.get(level).entries()) {
            entries.add(new BindingSyncPayload.Entry(
                    entry.id(),
                    entry.name(),
                    entry.center().asLong(),
                    entry.effectTicks(),
                    entry.paused(),
                    entry.powered(),
                    entry.stopping()));
        }

        return new BindingSyncPayload(level.getGameTime(), List.copyOf(entries));
    }
}
