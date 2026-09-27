package com.proxpero.syntacticwizardry;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
public final class ScribesLecternMenu extends AbstractContainerMenu {
 public static final int WIDTH=320,HEIGHT=232;
 private static final int COMPONENT_ACTION_STRIDE=64;
 private static final int PROPERTY_CELL_STRIDE=1024;
 private static final int PROPERTY_KEY_STRIDE=64;
 public static final int ACTION_ADD_COMPONENT_BASE=100;
 public static final int ACTION_MOVE_BASE=10000;
 public static final int ACTION_CLEAR_BASE=20000;
 public static final int ACTION_SET_PROPERTY_BASE=30000;
 public static final int ACTION_WRITE=70000;
 private static final int RADIUS_DATA_BASE=SpellPresentation.PLAN_DATA_SIZE;
 private static final int DAMAGE_KIND_DATA_BASE=RADIUS_DATA_BASE+SpellPresentation.CELLS;
 private static final int POTENCE_DATA_BASE=DAMAGE_KIND_DATA_BASE+SpellPresentation.CELLS;
 private static final int BOX_WIDTH_DATA_BASE=POTENCE_DATA_BASE+SpellPresentation.CELLS;
 private static final int BOX_HEIGHT_DATA_BASE=BOX_WIDTH_DATA_BASE+SpellPresentation.CELLS;
 private static final int BOX_DEPTH_DATA_BASE=BOX_HEIGHT_DATA_BASE+SpellPresentation.CELLS;
 private final Container paper=new SimpleContainer(1);
 private final ContainerData spellData=new SimpleContainerData(BOX_DEPTH_DATA_BASE+SpellPresentation.CELLS);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);
  addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
  int sx=80,sy=204;
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
  clearAllDefaults();
  addDataSlots(spellData);
 }
 public static int actionAddComponent(int cell,int type){return ACTION_ADD_COMPONENT_BASE+cell*COMPONENT_ACTION_STRIDE+type;}
 public static int actionMove(int from,int to){return ACTION_MOVE_BASE+from*SpellPresentation.CELLS+to;}
 public static int actionClear(int cell){return ACTION_CLEAR_BASE+cell;}
 public static int actionSetProperty(int cell,SpellPropertyKey key,int value){return ACTION_SET_PROPERTY_BASE+cell*PROPERTY_CELL_STRIDE+key.id()*PROPERTY_KEY_STRIDE+value;}
 private int off(int cell){return cell*SpellPresentation.STRIDE;}
 public int typeAt(int cell){return valid(cell)?spellData.get(off(cell)):SpellPresentation.TYPE_EMPTY;}
 public int styleAt(int cell){return valid(cell)?Mth.clamp(spellData.get(off(cell)+1),0,SpellPresentation.STYLE_COUNT-1):SpellPresentation.STYLE_DEFAULT;}
 public int visualAt(int cell){return valid(cell)?Mth.clamp(spellData.get(off(cell)+2),0,SpellPresentation.VISUAL_COUNT-1):SpellPresentation.VISUAL_DEFAULT;}
 public int radiusAt(int cell){return valid(cell)?Mth.clamp(spellData.get(RADIUS_DATA_BASE+cell),SpellPresentation.RADIUS_MIN,SpellPresentation.RADIUS_MAX):SpellPresentation.RADIUS_DEFAULT;}
 public int damageKindAt(int cell){return valid(cell)?Mth.clamp(spellData.get(DAMAGE_KIND_DATA_BASE+cell),0,SpellPresentation.DAMAGE_KIND_COUNT-1):SpellPresentation.DAMAGE_KIND_DEFAULT;}
 public int potenceAt(int cell){return valid(cell)?Mth.clamp(spellData.get(POTENCE_DATA_BASE+cell),SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX):SpellPresentation.POTENCE_DEFAULT;}
 public int boxWidthAt(int cell){return valid(cell)?Mth.clamp(spellData.get(BOX_WIDTH_DATA_BASE+cell),SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX):SpellPresentation.BOX_SIZE_DEFAULT;}
 public int boxHeightAt(int cell){return valid(cell)?Mth.clamp(spellData.get(BOX_HEIGHT_DATA_BASE+cell),SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX):SpellPresentation.BOX_SIZE_DEFAULT;}
 public int boxDepthAt(int cell){return valid(cell)?Mth.clamp(spellData.get(BOX_DEPTH_DATA_BASE+cell),SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX):SpellPresentation.BOX_SIZE_DEFAULT;}
 public int propertyValue(int cell,SpellPropertyKey key){
  return switch(key){
   case STYLE -> styleAt(cell);
   case VISUAL -> visualAt(cell);
   case RADIUS -> radiusAt(cell);
   case DAMAGE_KIND -> damageKindAt(cell);
   case POTENCE -> potenceAt(cell);
   case WIDTH -> boxWidthAt(cell);
   case HEIGHT -> boxHeightAt(cell);
   case DEPTH -> boxDepthAt(cell);
  };
 }
 public SpellComponentDefinition definitionAt(int cell){return SpellComponents.byType(typeAt(cell));}
 public boolean hasAny(){for(int i=0;i<SpellPresentation.CELLS;i++)if(typeAt(i)!=SpellPresentation.TYPE_EMPTY)return true;return false;}
 public int[] snapshotPlan(){int[] values=SpellPresentation.emptyPlan();for(int i=0;i<SpellPresentation.PLAN_DATA_SIZE;i++)values[i]=spellData.get(i);return values;}
 public int[] snapshotRadii(){int[] values=SpellPresentation.emptyRadii();for(int i=0;i<values.length;i++)values[i]=radiusAt(i);return values;}
 public int[] snapshotDamageKinds(){int[] values=SpellPresentation.emptyDamageKinds();for(int i=0;i<values.length;i++)values[i]=damageKindAt(i);return values;}
 public int[] snapshotPotences(){int[] values=SpellPresentation.emptyPotences();for(int i=0;i<values.length;i++)values[i]=potenceAt(i);return values;}
 public int[] snapshotBoxWidths(){int[] values=SpellPresentation.emptyBoxWidths();for(int i=0;i<values.length;i++)values[i]=boxWidthAt(i);return values;}
 public int[] snapshotBoxHeights(){int[] values=SpellPresentation.emptyBoxHeights();for(int i=0;i<values.length;i++)values[i]=boxHeightAt(i);return values;}
 public int[] snapshotBoxDepths(){int[] values=SpellPresentation.emptyBoxDepths();for(int i=0;i<values.length;i++)values[i]=boxDepthAt(i);return values;}
 private boolean valid(int cell){return cell>=0&&cell<SpellPresentation.CELLS;}
 private void clearAllDefaults(){for(int cell=0;cell<SpellPresentation.CELLS;cell++)resetPropertyDefaults(cell);}
 private void resetPropertyDefaults(int cell){
  int o=off(cell);
  spellData.set(o,SpellPresentation.TYPE_EMPTY);
  spellData.set(o+1,SpellPresentation.STYLE_DEFAULT);
  spellData.set(o+2,SpellPresentation.VISUAL_DEFAULT);
  spellData.set(RADIUS_DATA_BASE+cell,SpellPresentation.RADIUS_DEFAULT);
  spellData.set(DAMAGE_KIND_DATA_BASE+cell,SpellPresentation.DAMAGE_KIND_DEFAULT);
  spellData.set(POTENCE_DATA_BASE+cell,SpellPresentation.POTENCE_DEFAULT);
  spellData.set(BOX_WIDTH_DATA_BASE+cell,SpellPresentation.BOX_SIZE_DEFAULT);
  spellData.set(BOX_HEIGHT_DATA_BASE+cell,SpellPresentation.BOX_SIZE_DEFAULT);
  spellData.set(BOX_DEPTH_DATA_BASE+cell,SpellPresentation.BOX_SIZE_DEFAULT);
 }
 private void setCellType(int cell,int type){
  if(!valid(cell))return;
  resetPropertyDefaults(cell);
  if(type==SpellPresentation.TYPE_EMPTY)return;
  SpellComponentDefinition definition=SpellComponents.byType(type);
  if(definition==null)return;
  int o=off(cell);
  spellData.set(o,type);
  spellData.set(o+1,definition.defaultStyle());
  spellData.set(o+2,definition.defaultVisual());
  for(SpellPropertyDefinition property:definition.settings())setPropertyValue(cell,property.key(),property.defaultValue());
 }
 private void swap(int a,int b){
  if(!valid(a)||!valid(b)||a==b)return;
  for(int k=0;k<SpellPresentation.STRIDE;k++){int oa=off(a)+k,ob=off(b)+k,v=spellData.get(oa);spellData.set(oa,spellData.get(ob));spellData.set(ob,v);}
  swapData(RADIUS_DATA_BASE,a,b);swapData(DAMAGE_KIND_DATA_BASE,a,b);swapData(POTENCE_DATA_BASE,a,b);swapData(BOX_WIDTH_DATA_BASE,a,b);swapData(BOX_HEIGHT_DATA_BASE,a,b);swapData(BOX_DEPTH_DATA_BASE,a,b);
 }
 private void swapData(int base,int a,int b){int v=spellData.get(base+a);spellData.set(base+a,spellData.get(base+b));spellData.set(base+b,v);}
 private void setPropertyValue(int cell,SpellPropertyKey key,int value){
  if(!valid(cell))return;
  switch(key){
   case STYLE -> {if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+1,Mth.clamp(value,0,SpellPresentation.STYLE_COUNT-1));}
   case VISUAL -> {if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+2,Mth.clamp(value,0,SpellPresentation.VISUAL_COUNT-1));}
   case RADIUS -> {if(typeAt(cell)==SpellPresentation.TYPE_SPHERE)spellData.set(RADIUS_DATA_BASE+cell,Mth.clamp(value,SpellPresentation.RADIUS_MIN,SpellPresentation.RADIUS_MAX));}
   case DAMAGE_KIND -> {if(typeAt(cell)==SpellPresentation.TYPE_DAMAGE)spellData.set(DAMAGE_KIND_DATA_BASE+cell,Mth.clamp(value,0,SpellPresentation.DAMAGE_KIND_COUNT-1));}
   case POTENCE -> {if(typeAt(cell)==SpellPresentation.TYPE_DAMAGE)spellData.set(POTENCE_DATA_BASE+cell,Mth.clamp(value,SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX));}
   case WIDTH -> {if(typeAt(cell)==SpellPresentation.TYPE_BOX)spellData.set(BOX_WIDTH_DATA_BASE+cell,Mth.clamp(value,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX));}
   case HEIGHT -> {if(typeAt(cell)==SpellPresentation.TYPE_BOX)spellData.set(BOX_HEIGHT_DATA_BASE+cell,Mth.clamp(value,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX));}
   case DEPTH -> {if(typeAt(cell)==SpellPresentation.TYPE_BOX)spellData.set(BOX_DEPTH_DATA_BASE+cell,Mth.clamp(value,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX));}
  }
 }
 @Override public boolean clickMenuButton(Player player,int id){
  if(id>=ACTION_ADD_COMPONENT_BASE&&id<ACTION_MOVE_BASE){
   int code=id-ACTION_ADD_COMPONENT_BASE,cell=code/COMPONENT_ACTION_STRIDE,type=code%COMPONENT_ACTION_STRIDE;
   if(valid(cell)&&SpellComponents.byType(type)!=null){setCellType(cell,type);return true;}
  }
  if(id>=ACTION_MOVE_BASE&&id<ACTION_CLEAR_BASE){int code=id-ACTION_MOVE_BASE,from=code/SpellPresentation.CELLS,to=code%SpellPresentation.CELLS;swap(from,to);return true;}
  if(id>=ACTION_CLEAR_BASE&&id<ACTION_SET_PROPERTY_BASE){int cell=id-ACTION_CLEAR_BASE;setCellType(cell,SpellPresentation.TYPE_EMPTY);return true;}
  if(id>=ACTION_SET_PROPERTY_BASE&&id<ACTION_WRITE){
   int code=id-ACTION_SET_PROPERTY_BASE;
   int cell=code/PROPERTY_CELL_STRIDE;
   int rest=code%PROPERTY_CELL_STRIDE;
   SpellPropertyKey key=SpellPropertyKey.byId(rest/PROPERTY_KEY_STRIDE);
   int value=rest%PROPERTY_KEY_STRIDE;
   if(key!=null){setPropertyValue(cell,key,value);return true;}
  }
  if(id==ACTION_WRITE){
   if(!hasAny())return false;
   if(!player.getAbilities().instabuild){
    ItemStack paperStack=paper.getItem(0);
    if(!paperStack.is(Items.PAPER)||paperStack.isEmpty())return false;
    paperStack.shrink(1);paper.setChanged();
   }
   ItemStack written=WrittenSpellItem.create(snapshotPlan(),snapshotRadii(),snapshotDamageKinds(),snapshotPotences(),snapshotBoxWidths(),snapshotBoxHeights(),snapshotBoxDepths());
   if(!player.addItem(written))player.drop(written,false);
   return true;
  }
  return false;
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
