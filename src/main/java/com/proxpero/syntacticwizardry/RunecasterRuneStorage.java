package com.proxpero.syntacticwizardry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class RunecasterRuneStorage {
    private static final String RUNE_ID = "sw_runecaster_rune_id";
    private static final String RUNE_DATA = "sw_runecaster_rune_data";
    private static final String RUNE_NAME = "sw_runecaster_rune_name";

    private RunecasterRuneStorage() {}

    public static ItemStack getRune(ItemStack runecaster) {
        if (runecaster == null || runecaster.isEmpty()) return ItemStack.EMPTY;
        CompoundTag root = root(runecaster);
        String rawId = root.getString(RUNE_ID);
        if (rawId.isEmpty()) return ItemStack.EMPTY;

        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null) return ItemStack.EMPTY;

        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack rune = new ItemStack(item);
        if (!RunestoneItem.isRunestone(rune)) return ItemStack.EMPTY;

        if (root.contains(RUNE_DATA, Tag.TAG_COMPOUND)) {
            CustomData.set(DataComponents.CUSTOM_DATA, rune, root.getCompound(RUNE_DATA).copy());
        }

        String name = root.getString(RUNE_NAME);
        if (!name.isEmpty()) rune.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return rune;
    }

    public static void setRune(ItemStack runecaster, ItemStack rune) {
        if (runecaster == null || runecaster.isEmpty()) return;

        CompoundTag root = root(runecaster);
        if (rune == null || rune.isEmpty() || !RunestoneItem.isRunestone(rune) || !RunestoneItem.isCarved(rune)) {
            root.remove(RUNE_ID);
            root.remove(RUNE_DATA);
            root.remove(RUNE_NAME);
            write(runecaster, root);
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(rune.getItem());
        root.putString(RUNE_ID, id.toString());
        root.put(RUNE_DATA, rune.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());

        Component customName = rune.get(DataComponents.CUSTOM_NAME);
        if (customName != null) root.putString(RUNE_NAME, customName.getString());
        else root.remove(RUNE_NAME);

        write(runecaster, root);
    }

    private static CompoundTag root(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void write(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
