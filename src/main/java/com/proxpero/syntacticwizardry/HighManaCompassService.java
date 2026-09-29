package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class HighManaCompassService {
    private HighManaCompassService() {}

    public static HighManaClaimSavedData data(ServerPlayer player) {
        return player.server.overworld().getDataStorage().computeIfAbsent(HighManaClaimSavedData.factory(), HighManaClaimSavedData.DATA_NAME);
    }

    public static List<HighManaClaimSavedData.Claim> claims(ServerPlayer player) {
        return data(player).claims(player.getUUID());
    }

    public static void openMenu(ServerPlayer player) {
        List<HighManaClaimSavedData.Claim> claims = claims(player);
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new HighManaCompassMenu(containerId, inventory, claims),
                Component.literal("Claimed High Mana Zones")
        ), buffer -> {
            buffer.writeVarInt(claims.size());
            for (HighManaClaimSavedData.Claim claim : claims) {
                buffer.writeUtf(claim.dimension().toString());
                buffer.writeInt(claim.chunkX());
                buffer.writeInt(claim.chunkZ());
            }
        });
    }

    public static void syncTarget(ServerPlayer player) {
        if (!holdsCompass(player)) return;
        ServerLevel level = player.serverLevel();
        HighManaZones.Target target = HighManaZones.nearest(level, player);
        if (target == null) {
            PacketDistributor.sendToPlayer(player, new HighManaCompassStatePayload(false, level.dimension().location().toString(), 0, 0));
        } else {
            PacketDistributor.sendToPlayer(player, new HighManaCompassStatePayload(true, level.dimension().location().toString(), target.chunkX(), target.chunkZ()));
        }
    }

    public static boolean holdsCompass(ServerPlayer player) {
        return player.getMainHandItem().is(SyntacticWizardry.HIGH_MANA_COMPASS.get()) || player.getOffhandItem().is(SyntacticWizardry.HIGH_MANA_COMPASS.get());
    }

    public static void teleport(ServerPlayer player, HighManaClaimSavedData.Claim claim) {
        if (player == null || claim == null) return;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, claim.dimension());
        ServerLevel level = player.server.getLevel(key);
        if (level == null) {
            player.displayClientMessage(Component.literal("That dimension is unavailable."), true);
            return;
        }

        level.getChunk(claim.chunkX(), claim.chunkZ());
        Vec3 destination = findSafeDestination(level, player, claim.chunkX(), claim.chunkZ());
        if (destination == null) {
            player.displayClientMessage(Component.literal("No safe arrival point was found in that chunk."), true);
            return;
        }

        player.closeContainer();
        player.teleportTo(level, destination.x, destination.y, destination.z, java.util.Set.of(), player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static Vec3 findSafeDestination(ServerLevel level, ServerPlayer player, int chunkX, int chunkZ) {
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        int preferredY = level == player.level() ? player.getBlockY() : Math.max(level.getMinBuildHeight() + 2, Math.min(64, level.getMaxBuildHeight() - 3));
        Vec3 best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                for (int y = level.getMinBuildHeight() + 1; y < level.getMaxBuildHeight() - 2; y++) {
                    BlockPos feet = new BlockPos(x, y, z);
                    if (!safeFeet(level, player, feet)) continue;
                    double score = dx * dx + dz * dz + Math.abs(y - preferredY) * 0.15D;
                    if (score < bestScore) {
                        bestScore = score;
                        best = new Vec3(x + 0.5D, y, z + 0.5D);
                    }
                }
            }
        }
        return best;
    }

    private static boolean safeFeet(ServerLevel level, ServerPlayer player, BlockPos feet) {
        BlockPos ground = feet.below();
        BlockState groundState = level.getBlockState(ground);
        if (groundState.getCollisionShape(level, ground).isEmpty()) return false;
        AABB moved = player.getBoundingBox().move(new Vec3(feet.getX() + 0.5D - player.getX(), feet.getY() - player.getY(), feet.getZ() + 0.5D - player.getZ()));
        return level.noCollision(player, moved);
    }
}
