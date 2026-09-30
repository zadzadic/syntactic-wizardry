package com.proxpero.syntacticwizardry;

import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SpellRandomizerMenu extends AbstractContainerMenu {
    public static final int ACTION_RANDOMIZE = 1;
    public static final int SLOT_COUNT = 5;

    private final SimpleContainer generated = new SimpleContainer(SLOT_COUNT);

    public SpellRandomizerMenu(int id, Inventory inventory) {
        super(SyntacticWizardry.SPELL_RANDOMIZER_MENU.get(), id);

        int startX = 44;
        for (int i = 0; i < SLOT_COUNT; i++) {
            addSlot(new OutputSlot(generated, i, startX + i * 27, 52));
        }

        if (!inventory.player.level().isClientSide) randomize();
    }

    public void randomize() {
        RandomSource random = RandomSource.create();
        RandomSpellGenerator.Tier[] tiers = RandomSpellGenerator.Tier.values();

        for (int i = 0; i < SLOT_COUNT; i++) {
            RandomSpellGenerator.Tier tier = tiers[random.nextInt(tiers.length)];
            generated.setItem(i, RandomSpellGenerator.generate(tier, random));
        }
        generated.setChanged();
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != ACTION_RANDOMIZE || player.level().isClientSide) return false;
        randomize();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack original = slots.get(index).getItem();
        if (original.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = original.copy();
        player.getInventory().placeItemBackInInventory(copy);
        slots.get(index).set(ItemStack.EMPTY);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static final class OutputSlot extends Slot {
        OutputSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
