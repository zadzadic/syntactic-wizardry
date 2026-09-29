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
    public static final int ACTION_SEARCH_MODE = 1;
    public static final int ACTION_SELECT_BASE = 100;
    private final List<HighManaClaimSavedData.Claim> claims;
    private final boolean searchMode;
    private final HighManaClaimSavedData.Claim selectedTarget;

    public static HighManaCompassMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        boolean searchMode = data.readBoolean();
        HighManaClaimSavedData.Claim selected = null;
        if (data.readBoolean()) {
            ResourceLocation dimension = ResourceLocation.tryParse(data.readUtf());
            int chunkX = data.readInt();
            int chunkZ = data.readInt();
            if (dimension != null) selected = new HighManaClaimSavedData.Claim(dimension, chunkX, chunkZ);
        }
        int count = Math.max(0, Math.min(4096, data.readVarInt()));
        List<HighManaClaimSavedData.Claim> claims = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation dimension = ResourceLocation.tryParse(data.readUtf());
            int chunkX = data.readInt();
            int chunkZ = data.readInt();
            if (dimension != null) claims.add(new HighManaClaimSavedData.Claim(dimension, chunkX, chunkZ));
        }
        return new HighManaCompassMenu(id, inventory, claims, searchMode, selected);
    }

    public HighManaCompassMenu(int id, Inventory inventory, List<HighManaClaimSavedData.Claim> claims, boolean searchMode, HighManaClaimSavedData.Claim selectedTarget) {
        super(SyntacticWizardry.HIGH_MANA_COMPASS_MENU.get(), id);
        this.claims = List.copyOf(claims);
        this.searchMode = searchMode;
        this.selectedTarget = selectedTarget;
    }

    public List<HighManaClaimSavedData.Claim> claims() { return claims; }
    public boolean searchMode() { return searchMode; }
    public HighManaClaimSavedData.Claim selectedTarget() { return selectedTarget; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (id == ACTION_SEARCH_MODE) {
            HighManaCompassService.setSearchMode(serverPlayer);
            serverPlayer.closeContainer();
            return true;
        }

        int index = id - ACTION_SELECT_BASE;
        if (index < 0 || index >= claims.size()) return false;
        HighManaClaimSavedData.Claim requested = claims.get(index);
        if (!HighManaCompassService.claims(serverPlayer).contains(requested)) return false;
        if (!HighManaCompassService.selectClaim(serverPlayer, requested)) return false;
        HighManaCompassService.teleport(serverPlayer, requested);
        return true;
    }

    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
