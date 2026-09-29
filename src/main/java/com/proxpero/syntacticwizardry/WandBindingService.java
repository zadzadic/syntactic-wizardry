package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

public final class WandBindingService {
    private static final String TYPE = "sw_wand_binding_type";
    private static final String DIMENSION = "sw_wand_binding_dimension";
    private static final String POSITION = "sw_wand_binding_position";

    public record Binding(String type, ResourceLocation dimension, BlockPos position) {}

    private WandBindingService() {}

    public static void bindCore(ItemStack wand, ResourceLocation dimension, BlockPos position) {
        bind(wand, "core", dimension, position);
    }

    public static void bind(ItemStack wand, String type, ResourceLocation dimension, BlockPos position) {
        if (!isWand(wand) || dimension == null || position == null) return;
        CompoundTag tag = wand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(TYPE, type == null ? "" : type);
        tag.putString(DIMENSION, dimension.toString());
        tag.putLong(POSITION, position.asLong());
        CustomData.set(DataComponents.CUSTOM_DATA, wand, tag);
    }

    public static Binding get(ItemStack wand) {
        if (!isWand(wand)) return null;
        CompoundTag tag = wand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(DIMENSION) || !tag.contains(POSITION)) return null;
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(DIMENSION));
        if (dimension == null) return null;
        return new Binding(tag.getString(TYPE), dimension, BlockPos.of(tag.getLong(POSITION)));
    }

    public static boolean clear(ItemStack wand) {
        if (!isWand(wand)) return false;
        CompoundTag tag = wand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean had = tag.contains(DIMENSION) || tag.contains(POSITION) || tag.contains(TYPE);
        tag.remove(TYPE);
        tag.remove(DIMENSION);
        tag.remove(POSITION);
        CustomData.set(DataComponents.CUSTOM_DATA, wand, tag);
        return had;
    }

    private static boolean isWand(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(SyntacticWizardry.WAND.get());
    }
}
