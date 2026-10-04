package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ReliquaryRegistry {
    private static Block BLOCK;
    private static Item ITEM;
    private static BlockEntityType<ReliquaryBlockEntity> BLOCK_ENTITY_TYPE;

    private ReliquaryRegistry() {}

    public static Block block() {
        return BLOCK;
    }

    public static Item item() {
        return ITEM;
    }

    public static BlockEntityType<ReliquaryBlockEntity> blockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "reliquary");

        event.register(Registries.BLOCK, id, () -> {
            BLOCK = new ReliquaryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_BOOKSHELF));
            return BLOCK;
        });

        event.register(Registries.ITEM, id, () -> {
            ITEM = new BlockItem(BLOCK, new Item.Properties());
            return ITEM;
        });

        event.register(Registries.BLOCK_ENTITY_TYPE, id, () -> {
            BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(ReliquaryBlockEntity::new, BLOCK).build(null);
            return BLOCK_ENTITY_TYPE;
        });
    }

    @SubscribeEvent
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (ITEM != null && "syntacticwizardry:syntactic_wizardry".equals(event.getTabKey().location().toString())) {
            event.accept(new ItemStack(ITEM), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
