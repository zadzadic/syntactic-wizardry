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
    public static final int RUNE_SLOTS = 3;
    private static final String LEGACY_RUNE_ID = "sw_runecaster_rune_id";
    private static final String LEGACY_RUNE_DATA = "sw_runecaster_rune_data";
    private static final String LEGACY_RUNE_NAME = "sw_runecaster_rune_name";
    private static final String SELECTED = "sw_runecaster_selected_rune";

    private RunecasterRuneStorage() {}

    private static String idKey(int slot) { return "sw_runecaster_rune_" + slot + "_id"; }
    private static String dataKey(int slot) { return "sw_runecaster_rune_" + slot + "_data"; }
    private static String nameKey(int slot) { return "sw_runecaster_rune_" + slot + "_name"; }
    private static String rotationKey(int slot) { return "sw_runecaster_rune_" + slot + "_rotation"; }

    public static ItemStack getRune(ItemStack runecaster) {
        if (runecaster == null || runecaster.isEmpty()) return ItemStack.EMPTY;
        ItemStack firstMounted = ItemStack.EMPTY;
        int[] compiled = new int[RunestoneItem.MAX_CELLS];
        boolean any = false;

        for (int slot = 0; slot < RUNE_SLOTS; slot++) {
            ItemStack rune = getMountedRune(runecaster, slot);
            if (rune.isEmpty()) continue;
            if (firstMounted.isEmpty()) firstMounted = rune;

            int capacity = Math.max(1, RunestoneItem.slots(rune));
            int rotation = Math.floorMod(getRotation(runecaster, slot), capacity);
            int[] cells = RunestoneItem.cells(rune);
            int type = rotation < cells.length ? cells[rotation] : 0;
            compiled[slot] = type;
            if (type != 0) any = true;
        }

        if (!any || firstMounted.isEmpty()) return ItemStack.EMPTY;
        ItemStack synthetic = new ItemStack(firstMounted.getItem());
        RunestoneItem.carve(synthetic, compiled);
        synthetic.set(DataComponents.CUSTOM_NAME, Component.literal("Compiled Runestones"));
        return synthetic;
    }

    public static void setRune(ItemStack runecaster, ItemStack rune) {
        setMountedRune(runecaster, 0, rune);
    }

    public static ItemStack getMountedRune(ItemStack runecaster, int slot) {
        if (runecaster == null || runecaster.isEmpty() || slot < 0 || slot >= RUNE_SLOTS) return ItemStack.EMPTY;
        CompoundTag root = root(runecaster);

        String id = root.getString(idKey(slot));
        String storedDataKey = dataKey(slot);
        String storedNameKey = nameKey(slot);

        if (slot == 0 && id.isEmpty()) {
            id = root.getString(LEGACY_RUNE_ID);
            storedDataKey = LEGACY_RUNE_DATA;
            storedNameKey = LEGACY_RUNE_NAME;
        }

        if (id.isEmpty()) return ItemStack.EMPTY;
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return ItemStack.EMPTY;

        Item item = BuiltInRegistries.ITEM.get(key);
        ItemStack result = new ItemStack(item);
        if (!RunestoneItem.isRunestone(result)) return ItemStack.EMPTY;

        if (root.contains(storedDataKey, Tag.TAG_COMPOUND)) {
            CustomData.set(DataComponents.CUSTOM_DATA, result, root.getCompound(storedDataKey).copy());
        }
        String name = root.getString(storedNameKey);
        if (!name.isEmpty()) result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return result;
    }

    public static void setMountedRune(ItemStack runecaster, int slot, ItemStack rune) {
        if (runecaster == null || runecaster.isEmpty() || slot < 0 || slot >= RUNE_SLOTS) return;
        CompoundTag root = root(runecaster);

        if (rune == null || rune.isEmpty() || !RunestoneItem.isRunestone(rune) || !RunestoneItem.isCarved(rune)) {
            root.remove(idKey(slot));
            root.remove(dataKey(slot));
            root.remove(nameKey(slot));
            root.remove(rotationKey(slot));
        } else {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(rune.getItem());
            root.putString(idKey(slot), id.toString());
            root.put(dataKey(slot), rune.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());

            Component name = rune.get(DataComponents.CUSTOM_NAME);
            if (name != null) root.putString(nameKey(slot), name.getString());
            else root.remove(nameKey(slot));

            int capacity = Math.max(1, RunestoneItem.slots(rune));
            root.putInt(rotationKey(slot), Math.floorMod(root.getInt(rotationKey(slot)), capacity));
        }

        if (slot == 0) {
            root.remove(LEGACY_RUNE_ID);
            root.remove(LEGACY_RUNE_DATA);
            root.remove(LEGACY_RUNE_NAME);
        }
        write(runecaster, root);
    }

    public static int getRotation(ItemStack runecaster, int slot) {
        if (runecaster == null || runecaster.isEmpty() || slot < 0 || slot >= RUNE_SLOTS) return 0;
        ItemStack rune = getMountedRune(runecaster, slot);
        if (rune.isEmpty()) return 0;
        int capacity = Math.max(1, RunestoneItem.slots(rune));
        return Math.floorMod(root(runecaster).getInt(rotationKey(slot)), capacity);
    }

    public static void setRotation(ItemStack runecaster, int slot, int rotation) {
        if (runecaster == null || runecaster.isEmpty() || slot < 0 || slot >= RUNE_SLOTS) return;
        ItemStack rune = getMountedRune(runecaster, slot);
        if (rune.isEmpty()) return;
        int capacity = Math.max(1, RunestoneItem.slots(rune));
        CompoundTag root = root(runecaster);
        root.putInt(rotationKey(slot), Math.floorMod(rotation, capacity));
        write(runecaster, root);
    }

    public static int rotateSelected(ItemStack runecaster, int delta) {
        int selected = getSelectedSlot(runecaster);
        ItemStack rune = getMountedRune(runecaster, selected);
        if (rune.isEmpty()) return 0;
        setRotation(runecaster, selected, getRotation(runecaster, selected) + Integer.signum(delta));
        return getRotation(runecaster, selected);
    }

    public static int getSelectedSlot(ItemStack runecaster) {
        if (runecaster == null || runecaster.isEmpty()) return 0;
        return Math.floorMod(root(runecaster).getInt(SELECTED), RUNE_SLOTS);
    }

    public static void setSelectedSlot(ItemStack runecaster, int slot) {
        if (runecaster == null || runecaster.isEmpty()) return;
        CompoundTag root = root(runecaster);
        root.putInt(SELECTED, Math.floorMod(slot, RUNE_SLOTS));
        write(runecaster, root);
    }

    public static int cycleSelectedSlot(ItemStack runecaster, int delta) {
        if (runecaster == null || runecaster.isEmpty()) return 0;
        int direction = delta < 0 ? -1 : 1;
        int current = getSelectedSlot(runecaster);
        for (int i = 1; i <= RUNE_SLOTS; i++) {
            int candidate = Math.floorMod(current + direction * i, RUNE_SLOTS);
            if (!getMountedRune(runecaster, candidate).isEmpty()) {
                setSelectedSlot(runecaster, candidate);
                return candidate;
            }
        }
        return current;
    }

    public static int activeType(ItemStack runecaster, int slot) {
        ItemStack rune = getMountedRune(runecaster, slot);
        if (rune.isEmpty()) return 0;
        int[] cells = RunestoneItem.cells(rune);
        int rotation = getRotation(runecaster, slot);
        return rotation >= 0 && rotation < cells.length ? cells[rotation] : 0;
    }

    private static CompoundTag root(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void write(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
