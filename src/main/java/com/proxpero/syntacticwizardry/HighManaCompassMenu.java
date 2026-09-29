package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class HighManaCompassMenu extends AbstractContainerMenu {
    public static final int WIDTH = 340;
    public static final int HEIGHT = 220;
    public static final int ACTION_TELEPORT_BASE = 100;
    private final List<HighManaClaimSavedData.Claim> claims;

    public static HighManaCompassMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        int count = Math.max(0, Math.min(4096, data.readVarInt()));
        List<HighManaClaimSavedData.Claim> claims = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation dimension = ResourceLocation.tryParse(data.readUtf());
            int chunkX = data.readInt();
            int chunkZ = data.readInt();
            if (dimension != null) claims.add(new HighManaClaimSavedData.Claim(dimension, chunkX, chunkZ));
        }
        return new HighManaCompassMenu(id, inventory, claims);
    }

    public HighManaCompassMenu(int id, Inventory inventory, List<HighManaClaimSavedData.Claim> claims) {
        super(SyntacticWizardry.HIGH_MANA_COMPASS_MENU.get(), id);
        this.claims = List.copyOf(claims);
    }

    public List<HighManaClaimSavedData.Claim> claims() { return claims; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        int index = id - ACTION_TELEPORT_BASE;
        if (index < 0 || index >= claims.size()) return false;
        HighManaClaimSavedData.Claim requested = claims.get(index);
        if (!HighManaCompassService.claims(serverPlayer).contains(requested)) return false;
        HighManaCompassService.teleport(serverPlayer, requested);
        return true;
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
