package com.proxpero.syntacticwizardry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod(SyntacticWizardry.MOD_ID)
public final class SyntacticWizardry {
 public static final String MOD_ID="syntacticwizardry";
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
 private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MOD_ID);
 public static final DeferredBlock<Block> SCRIBES_LECTERN=BLOCKS.register("scribes_lectern",()->new Block(Block.Properties.ofFullCopy(Blocks.LECTERN)));
 public static final DeferredItem<BlockItem> SCRIBES_LECTERN_ITEM=ITEMS.register("scribes_lectern",()->new BlockItem(SCRIBES_LECTERN.get(),new Item.Properties()));
 public static final DeferredHolder<CreativeModeTab,CreativeModeTab> SYNTACTIC_WIZARDRY_TAB=CREATIVE_TABS.register("syntactic_wizardry",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.syntacticwizardry")).icon(()->SCRIBES_LECTERN_ITEM.get().getDefaultInstance()).displayItems((parameters,output)->output.accept(SCRIBES_LECTERN_ITEM.get())).build());
 public SyntacticWizardry(IEventBus modEventBus){BLOCKS.register(modEventBus);ITEMS.register(modEventBus);CREATIVE_TABS.register(modEventBus);}
}
