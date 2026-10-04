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
public final class ProtectionRitualEvents {
    // Component.literal returns MutableComponent in Minecraft 1.21.1.
    private ProtectionRitualEvents() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos center = event.getPos();
        ItemStack held = event.getItemStack();

        if (!held.is(SyntacticWizardry.WAND.get())) return;
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return;
        if (!(level instanceof ServerLevel server)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ProtectionPreparedAreaData preparedAreas = ProtectionPreparedAreaData.get(server);
        boolean committedArea = preparedAreas.hasOwner(player.getUUID());
        if (!ProtectionRitualStructure.hasPatternHint(server, center) && !committedArea) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (WandBindingService.get(held) != null) {
            player.displayClientMessage(Component.literal("The Wand must be empty to activate a Ritual."), true);
            return;
        }

        if (!ProtectionRitualStructure.detect(server, center)) {
            player.displayClientMessage(
                    Component.literal("The Protection Ritual pattern is incomplete or incorrect."), true);
            return;
        }

        ProtectionPreparedAreaData.Config prepared =
                preparedAreas.consume(player.getUUID());
        ProtectionRitualData.Entry entry = ProtectionRitualData.get(server).activate(
                server, center, player.getUUID(), prepared);

        server.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
        syncLevel(server);

        String area = entry.customArea()
                ? entry.shape().name().toLowerCase()
                : "20-block sphere";
        String state = entry.powered() ? "activating" : "dormant: no Mana supply";
        player.displayClientMessage(
                Component.literal("Protection Ritual activated (" + area + ", " + state + ")."), true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        ProtectionRitualData data = ProtectionRitualData.get(level);
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
        ProtectionRitualData data = ProtectionRitualData.get(level);

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
        ProtectionSyncPayload payload = payload(level);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static ProtectionSyncPayload payload(ServerLevel level) {
        List<ProtectionSyncPayload.Entry> entries = new ArrayList<>();
        for (ProtectionRitualData.Entry entry : ProtectionRitualData.get(level).entries()) {
            entries.add(new ProtectionSyncPayload.Entry(
                    entry.id(), entry.name(), entry.center().asLong(), entry.effectTicks(),
                    entry.paused(), entry.powered(), entry.stopping()));
        }
        return new ProtectionSyncPayload(level.getGameTime(), List.copyOf(entries));
    }
}