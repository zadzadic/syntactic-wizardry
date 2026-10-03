package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ConduitRegistry {
    private static Block BLOCK;
    private static Item ITEM;

    private ConduitRegistry() {}

    public static Block block() {
        return BLOCK;
    }

    public static Item item() {
        return ITEM;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "conduit");

        event.register(Registries.BLOCK, id, () -> {
            BLOCK = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK));
            return BLOCK;
        });

        event.register(Registries.ITEM, id, () -> {
            ITEM = new BlockItem(BLOCK, new Item.Properties());
            return ITEM;
        });
    }

    @SubscribeEvent
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (ITEM != null && "syntacticwizardry:syntactic_wizardry".equals(event.getTabKey().location().toString())) {
            event.accept(new ItemStack(ITEM), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
