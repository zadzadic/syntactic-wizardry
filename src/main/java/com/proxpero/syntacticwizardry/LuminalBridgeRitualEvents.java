package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LuminalBridgeRitualEvents {
    private LuminalBridgeRitualEvents() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos center = event.getPos();
        ItemStack held = event.getItemStack();

        if (!held.is(SyntacticWizardry.WAND.get())) return;
        if (!level.getBlockState(center).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return;
        if (ReliquaryRegistry.block() == null || !level.getBlockState(center.below()).is(ReliquaryRegistry.block())) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        if (!(level instanceof ServerLevel server) || !(event.getEntity() instanceof ServerPlayer player)) return;

        if (WandBindingService.get(held) != null) {
            player.displayClientMessage(Component.literal("The Wand must be empty to activate a Ritual."), true);
            return;
        }

        LuminalBridgeStructure.Detection detection = LuminalBridgeStructure.detect(server, center);
        if (!detection.valid()) {
            player.displayClientMessage(Component.literal(detection.error()), true);
            return;
        }

        LuminalBridgeData.Entry entry = LuminalBridgeData.get(server.getServer()).activate(server, center, detection.geometry());
        server.setBlock(center, Blocks.AIR.defaultBlockState(), 3);
        syncAll(server.getServer());

        String state = entry.powered() ? "active" : "dormant: no Mana supply";
        player.displayClientMessage(
                Component.literal("Luminal Bridge activated ("
                        + detection.geometry().width() + "x" + detection.geometry().height()
                        + ", " + state + ")."),
                true);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (LuminalBridgeData.get(server).tick(server)) syncAll(server);
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

    public static void openDestinations(ServerPlayer player, BlockPos portalPos) {
        if (player == null || portalPos == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        LuminalBridgeData data = LuminalBridgeData.get(server);
        LuminalBridgeData.Entry source = data.findByPortal(player.serverLevel(), portalPos);
        if (source == null || !source.active()) return;

        List<LuminalBridgeDestinationsPayload.Destination> destinations = new ArrayList<>();
        for (LuminalBridgeData.Entry entry : data.entries()) {
            if (entry.id().equals(source.id()) || !entry.active() || !data.hasCodex(server, entry)) continue;
            String label = data.destinationLabel(server, entry);
            if (label.isBlank()) continue;
            BlockPos center = entry.center();
            destinations.add(new LuminalBridgeDestinationsPayload.Destination(
                    entry.id(),
                    label,
                    entry.dimension().toString(),
                    center.getX(),
                    center.getY(),
                    center.getZ()));
        }

        destinations.sort(java.util.Comparator.comparing(
                LuminalBridgeDestinationsPayload.Destination::label,
                String.CASE_INSENSITIVE_ORDER));

        PacketDistributor.sendToPlayer(
                player,
                new LuminalBridgeDestinationsPayload(source.id(), List.copyOf(destinations)));
    }

    public static void travel(ServerPlayer player, UUID sourceId, UUID destinationId) {
        if (player == null || sourceId == null || destinationId == null) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        LuminalBridgeData data = LuminalBridgeData.get(server);
        LuminalBridgeData.Entry source = data.find(sourceId);
        LuminalBridgeData.Entry destination = data.find(destinationId);
        if (source == null || destination == null || source.id().equals(destination.id())) return;
        if (!source.active() || !destination.active() || !data.hasCodex(server, destination)) return;
        if (!player.serverLevel().dimension().location().equals(source.dimension())) return;

        Vec3 sourceCenter = Vec3.atCenterOf(source.geometry().centerBlock());
        if (player.position().distanceToSqr(sourceCenter) > 64.0D) return;

        ServerLevel destinationLevel = LuminalBridgeData.level(server, destination.dimension());
        if (destinationLevel == null) return;

        LuminalBridgeStructure.PortalGeometry geometry = destination.geometry();
        BlockPos portalCenter = geometry.centerBlock();
        Vec3 target = destinationPoint(geometry, portalCenter);
        float yaw = geometry.front().getAxis() == net.minecraft.core.Direction.Axis.Y
                ? player.getYRot()
                : geometry.front().toYRot();
        player.teleportTo(destinationLevel, target.x, target.y, target.z, Set.of(), yaw, player.getXRot());
    }

    public static void handleControl(ServerPlayer player, RitualControlPayload payload) {
        if (player == null || payload == null || player.getServer() == null) return;
        MinecraftServer server = player.getServer();
        LuminalBridgeData data = LuminalBridgeData.get(server);

        boolean changed = switch (payload.action()) {
            case "pause" -> data.setPaused(server, payload.id(), true);
            case "resume" -> data.setPaused(server, payload.id(), false);
            case "stop" -> data.stop(server, payload.id());
            case "rename" -> data.rename(payload.id(), payload.value());
            default -> false;
        };

        if (changed) syncAll(server);
    }

    public static void syncPlayer(ServerPlayer player) {
        if (player == null || player.getServer() == null) return;
        PacketDistributor.sendToPlayer(player, syncPayload(player.getServer()));
    }

    public static void syncAll(MinecraftServer server) {
        if (server == null) return;
        LuminalBridgeSyncPayload payload = syncPayload(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static LuminalBridgeSyncPayload syncPayload(MinecraftServer server) {
        List<LuminalBridgeSyncPayload.Entry> entries = new ArrayList<>();
        for (LuminalBridgeData.Entry entry : LuminalBridgeData.get(server).entries()) {
            entries.add(new LuminalBridgeSyncPayload.Entry(
                    entry.id(),
                    entry.name(),
                    entry.dimension().toString(),
                    entry.center().asLong(),
                    entry.paused(),
                    entry.powered()));
        }
        return new LuminalBridgeSyncPayload(List.copyOf(entries));
    }

    private static Vec3 destinationPoint(
            LuminalBridgeStructure.PortalGeometry geometry,
            BlockPos center) {
        double x = center.getX() + 0.5D;
        double y = center.getY() + 0.1D;
        double z = center.getZ() + 0.5D;
        Vec3 normal = Vec3.atLowerCornerOf(geometry.front().getNormal());

        if (geometry.front().getAxis() == net.minecraft.core.Direction.Axis.Y) {
            y = geometry.front() == net.minecraft.core.Direction.UP
                    ? geometry.max().getY() + 1.1D
                    : geometry.min().getY() - 1.1D;
        } else {
            y = geometry.min().getY() + 0.1D;
        }

        return new Vec3(x, y, z).add(normal.scale(1.35D));
    }
}
