package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@EventBusSubscriber(modid=SyntacticWizardry.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
public final class ChalkRegistry {
    private static final Map<DyeColor,Item> ITEMS=new EnumMap<>(DyeColor.class);
    private static ChalkRuneBlock BLOCK;

    private ChalkRegistry(){}

    public static ChalkRuneBlock block(){return BLOCK;}
    public static Item item(DyeColor color){return ITEMS.get(color);}
    public static Item[] items(){return Arrays.stream(DyeColor.values()).map(ITEMS::get).filter(java.util.Objects::nonNull).toArray(Item[]::new);}
    public static String itemPath(DyeColor color){return color==DyeColor.WHITE?"chalk":color.getSerializedName()+"_chalk";}

    @SubscribeEvent
    public static void register(RegisterEvent event){
        ResourceLocation blockId=ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID,"chalk_rune");
        event.register(Registries.BLOCK,blockId,()->{
            BLOCK=new ChalkRuneBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().instabreak().replaceable().noLootTable());
            return BLOCK;
        });

        for(DyeColor color:DyeColor.values()){
            ResourceLocation id=ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID,itemPath(color));
            event.register(Registries.ITEM,id,()->{
                Item item=new ChalkItem(new Item.Properties().durability(128),color);
                ITEMS.put(color,item);
                return item;
            });
        }
    }

    @SubscribeEvent
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event){
        if(!"syntacticwizardry:syntactic_wizardry".equals(event.getTabKey().location().toString())) return;
        for(DyeColor color:DyeColor.values()){
            Item item=ITEMS.get(color);
            if(item!=null) event.accept(new ItemStack(item),CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
