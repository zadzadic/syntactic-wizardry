package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class GoldenOrbMenu extends AbstractContainerMenu {
    public static final int ACTION_MINUS_TEN = 1;
    public static final int ACTION_MINUS_ONE = 2;
    public static final int ACTION_PLUS_ONE = 3;
    public static final int ACTION_PLUS_TEN = 4;

    public GoldenOrbMenu(int id, Inventory inventory) {
        super(SyntacticWizardry.GOLDEN_ORB_MENU.get(), id);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isCreative();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!player.isCreative()) return false;
        int delta = switch (id) {
            case ACTION_MINUS_TEN -> -10;
            case ACTION_MINUS_ONE -> -1;
            case ACTION_PLUS_ONE -> 1;
            case ACTION_PLUS_TEN -> 10;
            default -> 0;
        };
        if (delta == 0) return false;
        ManaService.setCastingLevel(player, Math.max(1, ManaService.getCastingLevel(player) + delta));
        return true;
    }
}
