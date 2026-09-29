package com.proxpero.syntacticwizardry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
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
 private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,MOD_ID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,MOD_ID);
 private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MOD_ID);
 public static final DeferredBlock<Block> SCRIBES_LECTERN=BLOCKS.register("scribes_lectern",()->new ScribesLecternBlock(Block.Properties.ofFullCopy(Blocks.LECTERN)));
 public static final DeferredBlock<RuneBlock> RUNE_BLOCK=BLOCKS.register("rune",()->new RuneBlock(Block.Properties.of().noCollission().noOcclusion().strength(0.0F)));
 public static final DeferredBlock<Block> TEMPORARY_BLOCK=BLOCKS.register("temporary_block",()->new Block(Block.Properties.of().instabreak().noOcclusion().noLootTable().isSuffocating((state,level,pos)->false).isViewBlocking((state,level,pos)->false).isRedstoneConductor((state,level,pos)->false)));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<RuneBlockEntity>> RUNE_BLOCK_ENTITY=BLOCK_ENTITY_TYPES.register("rune",()->BlockEntityType.Builder.of(RuneBlockEntity::new,RUNE_BLOCK.get()).build(null));
 public static final DeferredItem<BlockItem> SCRIBES_LECTERN_ITEM=ITEMS.register("scribes_lectern",()->new BlockItem(SCRIBES_LECTERN.get(),new Item.Properties()));
 public static final DeferredItem<BlockItem> TEMPORARY_BLOCK_ITEM=ITEMS.register("temporary_block",()->new BlockItem(TEMPORARY_BLOCK.get(),new Item.Properties()));
 public static final DeferredHolder<EntityType<?>,EntityType<SpellMissile>> SPELL_MISSILE=ENTITIES.register("spell_missile",()->EntityType.Builder.<SpellMissile>of(SpellMissile::new,MobCategory.MISC).sized(0.25F,0.25F).clientTrackingRange(8).updateInterval(1).build("spell_missile"));
 public static final DeferredHolder<EntityType<?>,EntityType<SphereVisualEntity>> SPHERE_VISUAL=ENTITIES.register("sphere_visual",()->EntityType.Builder.<SphereVisualEntity>of(SphereVisualEntity::new,MobCategory.MISC).sized(0.1F,0.1F).clientTrackingRange(32).updateInterval(1).noSave().noSummon().build("sphere_visual"));
 public static final DeferredHolder<EntityType<?>,EntityType<BoxVisualEntity>> BOX_VISUAL=ENTITIES.register("box_visual",()->EntityType.Builder.<BoxVisualEntity>of(BoxVisualEntity::new,MobCategory.MISC).sized(0.1F,0.1F).clientTrackingRange(32).updateInterval(1).noSave().noSummon().build("box_visual"));
 public static final DeferredHolder<EntityType<?>,EntityType<ConeVisualEntity>> CONE_VISUAL=ENTITIES.register("cone_visual",()->EntityType.Builder.<ConeVisualEntity>of(ConeVisualEntity::new,MobCategory.MISC).sized(0.1F,0.1F).clientTrackingRange(32).updateInterval(1).noSave().noSummon().build("cone_visual"));
 public static final DeferredHolder<EntityType<?>,EntityType<PointVisualEntity>> POINT_VISUAL=ENTITIES.register("point_visual",()->EntityType.Builder.<PointVisualEntity>of(PointVisualEntity::new,MobCategory.MISC).sized(0.1F,0.1F).clientTrackingRange(32).updateInterval(1).noSave().noSummon().build("point_visual"));
 public static final DeferredItem<MissileShapeItem> MISSILE_SHAPE=ITEMS.register("missile_shape",()->new MissileShapeItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> SPHERE_SHAPE=ITEMS.register("sphere_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> BOX_SHAPE=ITEMS.register("box_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> CONE_SHAPE=ITEMS.register("cone_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> FLOATING_SHAPE=ITEMS.register("floating_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> TOUCH_SHAPE=ITEMS.register("touch_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> TARGET_SHAPE=ITEMS.register("target_shape",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> DAMAGE_EFFECT=ITEMS.register("damage_effect",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> DIG_EFFECT=ITEMS.register("dig_effect",()->new Item(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<WrittenSpellItem> WRITTEN_SPELL=ITEMS.register("written_spell",()->new WrittenSpellItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<HighManaCompassItem> HIGH_MANA_COMPASS=ITEMS.register("high_mana_compass",()->new HighManaCompassItem(new Item.Properties().stacksTo(1)));
 public static final DeferredHolder<MenuType<?>,MenuType<ScribesLecternMenu>> SCRIBES_LECTERN_MENU=MENUS.register("scribes_lectern",()->IMenuTypeExtension.create(SyntacticWizardry::createScribesLecternMenu));
 public static final DeferredHolder<MenuType<?>,MenuType<DimensionalStorageMenu>> DIMENSIONAL_STORAGE_MENU=MENUS.register("dimensional_storage",()->IMenuTypeExtension.create(DimensionalStorageMenu::client));
 public static final DeferredHolder<MenuType<?>,MenuType<HighManaCompassMenu>> HIGH_MANA_COMPASS_MENU=MENUS.register("high_mana_compass",()->IMenuTypeExtension.create(HighManaCompassMenu::client));
 public static final DeferredHolder<CreativeModeTab,CreativeModeTab> SYNTACTIC_WIZARDRY_TAB=CREATIVE_TABS.register("syntactic_wizardry",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.syntacticwizardry")).icon(()->SCRIBES_LECTERN_ITEM.get().getDefaultInstance()).displayItems((parameters,output)->{output.accept(SCRIBES_LECTERN_ITEM.get());output.accept(TEMPORARY_BLOCK_ITEM.get());output.accept(MISSILE_SHAPE.get());output.accept(SPHERE_SHAPE.get());output.accept(BOX_SHAPE.get());output.accept(CONE_SHAPE.get());output.accept(FLOATING_SHAPE.get());output.accept(TOUCH_SHAPE.get());output.accept(TARGET_SHAPE.get());output.accept(DAMAGE_EFFECT.get());output.accept(DIG_EFFECT.get());output.accept(HIGH_MANA_COMPASS.get());}).build());
 private static ScribesLecternMenu createScribesLecternMenu(int id,Inventory inv,RegistryFriendlyByteBuf data){return new ScribesLecternMenu(id,inv);}
 public SyntacticWizardry(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);ENTITIES.register(bus);BLOCK_ENTITY_TYPES.register(bus);MENUS.register(bus);CREATIVE_TABS.register(bus);}
}
