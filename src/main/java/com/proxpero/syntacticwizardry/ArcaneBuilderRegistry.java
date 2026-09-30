package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "syntacticwizardry", bus = EventBusSubscriber.Bus.MOD)
public final class ArcaneBuilderRegistry {
    private static Block BLOCK;
    private static Item ITEM;

    private ArcaneBuilderRegistry() {}

    public static Block block() {
        return BLOCK;
    }

    public static Item item() {
        return ITEM;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("syntacticwizardry", "arcane_builder");
        event.register(Registries.BLOCK, id, () -> {
            BLOCK = new com.arcane.magic.block.ArcaneBuilderBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion());
            return BLOCK;
        });
        event.register(Registries.ITEM, id, () -> {
            ITEM = new com.arcane.magic.item.ArcaneBuilderItem(BLOCK, new Item.Properties().stacksTo(1).durability(100));
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
