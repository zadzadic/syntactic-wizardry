package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Shared loot/tool logic for spell-driven block breaking. */
public final class BlockBreakService {
    private BlockBreakService() {}

    public static List<ItemStack> calculateDrops(ServerLevel level, Entity owner, BlockPos pos,
                                                  BlockState state, BlockEntity blockEntity, int potence) {
        int tier = Math.max(1, Math.min(5, potence));
        ItemStack tool = toolFor(state, tier);
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack drop : Block.getDrops(state, level, pos, blockEntity, owner, tool)) {
            if (drop != null && !drop.isEmpty()) result.add(drop.copy());
        }
        return result;
    }

    public static void spawnDrops(ServerLevel level, BlockPos pos, List<ItemStack> drops) {
        if (drops == null) return;
        for (ItemStack drop : drops) {
            if (drop != null && !drop.isEmpty()) Block.popResource(level, pos, drop.copy());
        }
    }

    public static ItemStack toolFor(BlockState state, int tier) {
        if (tier <= 1) return stack(Items.WOODEN_SHOVEL);
        boolean shovel = state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        boolean axe = state.is(BlockTags.MINEABLE_WITH_AXE);
        boolean hoe = state.is(BlockTags.MINEABLE_WITH_HOE);
        return switch (tier) {
            case 2 -> stack(shovel ? Items.WOODEN_SHOVEL : axe ? Items.WOODEN_AXE : hoe ? Items.WOODEN_HOE : Items.WOODEN_PICKAXE);
            case 3 -> stack(shovel ? Items.STONE_SHOVEL : axe ? Items.STONE_AXE : hoe ? Items.STONE_HOE : Items.STONE_PICKAXE);
            case 4 -> stack(shovel ? Items.IRON_SHOVEL : axe ? Items.IRON_AXE : hoe ? Items.IRON_HOE : Items.IRON_PICKAXE);
            default -> stack(shovel ? Items.DIAMOND_SHOVEL : axe ? Items.DIAMOND_AXE : hoe ? Items.DIAMOND_HOE : Items.DIAMOND_PICKAXE);
        };
    }

    private static ItemStack stack(ItemLike item) {
        return new ItemStack(item);
    }
}
