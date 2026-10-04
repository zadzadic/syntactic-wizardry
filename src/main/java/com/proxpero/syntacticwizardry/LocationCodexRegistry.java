package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class LocationCodexRegistry {
    private static Item ITEM;

    private LocationCodexRegistry() {}

    public static Item item() {
        return ITEM;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "location_codex");
        event.register(Registries.ITEM, id, () -> {
            ITEM = new Item(new Item.Properties().stacksTo(1));
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
