package com.proxpero.syntacticwizardry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class FocusSpellStorage {
    private static final String ACTIVE = "sw_focus_active";
    private static final String SPELL_PREFIX = "sw_focus_spell_";
    private static final String NAME_PREFIX = "sw_focus_name_";

    private FocusSpellStorage() {}

    public static int slotCount(ItemStack focus) {
        return focus != null && focus.getItem() instanceof MagicFocusItem item ? item.spellSlots() : 0;
    }

    public static int activeSlot(ItemStack focus) {
        int count = slotCount(focus);
        if (count <= 0) return 0;
        int stored = tag(focus).getInt(ACTIVE);
        return Math.max(0, Math.min(count - 1, stored));
    }

    public static void setActiveSlot(ItemStack focus, int slot) {
        int count = slotCount(focus);
        if (count <= 0) return;
        int normalized = Math.floorMod(slot, count);
        CompoundTag tag = tag(focus);
        tag.putInt(ACTIVE, normalized);
        writeTag(focus, tag);
    }

    public static ItemStack getActiveSpell(ItemStack focus) {
        return getSlot(focus, activeSlot(focus));
    }

    public static ItemStack getSlot(ItemStack focus, int slot) {
        if (slot < 0 || slot >= slotCount(focus)) return ItemStack.EMPTY;
        CompoundTag root = tag(focus);
        String key = SPELL_PREFIX + slot;
        if (!root.contains(key, Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
        CompoundTag stored = root.getCompound(key);
        if (stored.isEmpty()) return ItemStack.EMPTY;
        ItemStack spell = new ItemStack(SyntacticWizardry.WRITTEN_SPELL.get());
        CustomData.set(DataComponents.CUSTOM_DATA, spell, stored.copy());
        String name = root.getString(NAME_PREFIX + slot);
        if (!name.isEmpty()) spell.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return spell;
    }

    public static void setSlot(ItemStack focus, int slot, ItemStack spell) {
        if (slot < 0 || slot >= slotCount(focus)) return;
        CompoundTag root = tag(focus);
        String spellKey = SPELL_PREFIX + slot;
        String nameKey = NAME_PREFIX + slot;
        if (spell == null || spell.isEmpty() || !spell.is(SyntacticWizardry.WRITTEN_SPELL.get())) {
            root.remove(spellKey);
            root.remove(nameKey);
        } else {
            CompoundTag spellData = spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            root.put(spellKey, spellData);
            root.putString(nameKey, spell.getHoverName().getString());
        }
        writeTag(focus, root);
    }

    public static ItemStack heldFocus(Player player) {
        if (player == null) return ItemStack.EMPTY;
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof MagicFocusItem) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof MagicFocusItem) return off;
        return ItemStack.EMPTY;
    }

    public static void cycleHeldFocus(Player player, int delta) {
        ItemStack focus = heldFocus(player);
        if (focus.isEmpty()) return;
        int count = slotCount(focus);
        if (count <= 1) return;
        int next = Math.floorMod(activeSlot(focus) + (delta < 0 ? -1 : 1), count);
        setActiveSlot(focus, next);
        player.getInventory().setChanged();
        ItemStack spell = getSlot(focus, next);
        String name = spell.isEmpty() ? "Empty" : spell.getHoverName().getString();
        player.displayClientMessage(Component.literal("Slot " + (next + 1) + ": " + name), true);
    }

    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void writeTag(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
