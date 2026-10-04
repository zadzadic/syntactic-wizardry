package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ReliquaryBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public ReliquaryBlockEntity(BlockPos pos, BlockState state) {
        super(ReliquaryRegistry.blockEntityType(), pos, state);
    }

    public boolean isEmpty() {
        return items.getFirst().isEmpty();
    }

    public ItemStack storedCodex() {
        return items.getFirst();
    }

    public boolean insert(ItemStack stack) {
        if (stack == null || stack.isEmpty() || LocationCodexRegistry.item() == null) return false;
        if (!stack.is(LocationCodexRegistry.item()) || !isEmpty()) return false;
        items.set(0, stack.copyWithCount(1));
        setChanged();
        return true;
    }

    public ItemStack take() {
        ItemStack result = items.getFirst();
        if (result.isEmpty()) return ItemStack.EMPTY;
        items.set(0, ItemStack.EMPTY);
        setChanged();
        return result;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
