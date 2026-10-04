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
public final class CatchTimeRitualEvents {
    private static final long DAY_LENGTH = 24000L;

    private CatchTimeRitualEvents() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos center = event.getPos();
        ItemStack held = event.getItemStack();

        if (!held.is(SyntacticWizardry.WAND.get())) return;
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return;
        if (!(level instanceof ServerLevel server)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Lapis directly beneath the crystal identifies Catch Time strongly enough to run
        // the full validator. Do not hide validation failures behind a heuristic match count.
        if (!server.getBlockState(center.below()).is(Blocks.LAPIS_BLOCK)) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (WandBindingService.get(held) != null) {
            player.displayClientMessage(Component.literal("The Wand must be empty to activate a Ritual."), true);
            return;
        }

        CatchTimeRitualStructure.Detection detection = CatchTimeRitualStructure.detect(server, center);
        if (!detection.valid()) {
            player.displayClientMessage(Component.literal(detection.error()), true);
            return;
        }

        CatchTimeRitualData.Entry entry = CatchTimeRitualData.get(server).activate(
                server, center, detection.setting());
        server.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
        syncLevel(server);

        String state = entry.powered() ? "activating" : "dormant: no Mana supply";
        player.displayClientMessage(Component.literal(
                "Catch Time Ritual activated for " + entry.setting().displayName() + " (" + state + ")."), true);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        CatchTimeRitualData data = CatchTimeRitualData.get(level);
        boolean changed = data.tick(level);
        applyTimeLock(level, data.currentSettingOverride());
        if (changed) syncLevel(level);
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
        CatchTimeRitualData data = CatchTimeRitualData.get(level);

        boolean changed = switch (payload.action()) {
            case "pause" -> data.setPaused(payload.id(), true);
            case "resume" -> data.setPaused(payload.id(), false);
            case "stop" -> data.stop(payload.id());
            case "rename" -> data.rename(payload.id(), payload.value());
            default -> false;
        };

        if (!changed) return;
        applyTimeLock(level, data.currentSettingOverride());
        syncLevel(level);
    }

    public static void syncPlayer(ServerPlayer player) {
        if (player == null) return;
        PacketDistributor.sendToPlayer(player, payload(player.serverLevel()));
    }

    public static void syncLevel(ServerLevel level) {
        CatchTimeSyncPayload payload = payload(level);
        for (ServerPlayer player : level.players()) PacketDistributor.sendToPlayer(player, payload);
    }

    private static CatchTimeSyncPayload payload(ServerLevel level) {
        List<CatchTimeSyncPayload.Entry> entries = new ArrayList<>();
        for (CatchTimeRitualData.Entry entry : CatchTimeRitualData.get(level).entries()) {
            entries.add(new CatchTimeSyncPayload.Entry(
                    entry.id(), entry.name(), entry.center().asLong(), entry.setting().ordinal(),
                    entry.effectTicks(), entry.paused(), entry.powered(), entry.stopping(), entry.effectApplied()));
        }
        return new CatchTimeSyncPayload(level.getGameTime(), List.copyOf(entries));
    }

    private static void applyTimeLock(ServerLevel level, CatchTimeSetting setting) {
        if (setting == null) return;
        long current = level.getDayTime();
        long day = Math.floorDiv(current, DAY_LENGTH);
        level.setDayTime(day * DAY_LENGTH + setting.dayTime());
    }
}