package com.proxpero.syntacticwizardry;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class HighManaCompassItem extends Item {
    public HighManaCompassItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        claimCurrent(level, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        claimCurrent(context.getLevel(), player);
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private static void claimCurrent(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
        int chunkX = player.chunkPosition().x;
        int chunkZ = player.chunkPosition().z;
        if (!HighManaZones.isHighMana(serverLevel, chunkX, chunkZ)) {
            player.displayClientMessage(Component.literal("No High Mana Zone detected in this chunk."), true);
            return;
        }
        HighManaClaimSavedData data = HighManaCompassService.data(serverPlayer);
        HighManaClaimSavedData.Claim claim = new HighManaClaimSavedData.Claim(serverLevel.dimension().location(), chunkX, chunkZ);
        if (data.claim(serverPlayer.getUUID(), claim)) {
            player.displayClientMessage(Component.literal("High Mana Zone claimed at " + claim.blockX() + ", " + claim.blockZ() + "."), true);
        } else {
            player.displayClientMessage(Component.literal("This High Mana Zone is already claimed."), true);
        }
        HighManaCompassService.syncTarget(serverPlayer);
    }
}
