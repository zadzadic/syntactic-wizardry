package com.proxpero.syntacticwizardry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod(SyntacticWizardry.MOD_ID)
public final class SyntacticWizardry {
 public static final String MOD_ID="syntacticwizardry";
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(MOD_ID);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(MOD_ID);
 private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,MOD_ID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD_ID);
 private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MOD_ID);
 public static final DeferredBlock<Block> SCRIBES_LECTERN=BLOCKS.register("scribes_lectern",()->new ScribesLecternBlock(Block.Properties.ofFullCopy(Blocks.LECTERN)));
 public static final DeferredItem<BlockItem> SCRIBES_LECTERN_ITEM=ITEMS.register("scribes_lectern",()->new BlockItem(SCRIBES_LECTERN.get(),new Item.Properties()));
 public static final DeferredHolder<EntityType<?>,EntityType<SpellMissile>> SPELL_MISSILE=ENTITIES.register("spell_missile",()->EntityType.Builder.<SpellMissile>of(SpellMissile::new,MobCategory.MISC).sized(0.25F,0.25F).clientTrackingRange(4).updateInterval(10).build("spell_missile"));
 public static final DeferredItem<MissileShapeItem> MISSILE_SHAPE=ITEMS.register("missile_shape",()->new MissileShapeItem(new Item.Properties().stacksTo(1)));
 public static final DeferredHolder<MenuType<?>,MenuType<ScribesLecternMenu>> SCRIBES_LECTERN_MENU=MENUS.register("scribes_lectern",()->IMenuTypeExtension.create((id,inv,data)->new ScribesLecternMenu(id,inv)));
 public static final DeferredHolder<CreativeModeTab,CreativeModeTab> SYNTACTIC_WIZARDRY_TAB=CREATIVE_TABS.register("syntactic_wizardry",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.syntacticwizardry")).icon(()->SCRIBES_LECTERN_ITEM.get().getDefaultInstance()).displayItems((parameters,output)->{output.accept(SCRIBES_LECTERN_ITEM.get());output.accept(MISSILE_SHAPE.get());}).build());
 public SyntacticWizardry(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);MENUS.register(bus);CREATIVE_TABS.register(bus);}
}
