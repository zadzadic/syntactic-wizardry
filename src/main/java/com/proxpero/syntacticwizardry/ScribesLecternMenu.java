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
 public static final int ACTION_ADD_MISSILE_BASE=100,ACTION_ADD_SPHERE_BASE=200,ACTION_ADD_DAMAGE_BASE=300,ACTION_MOVE_BASE=1000,ACTION_CLEAR_BASE=2000,ACTION_STYLE_BASE=3000,ACTION_VISUAL_BASE=4000,ACTION_WRITE=5000,ACTION_RADIUS_BASE=6000,ACTION_DAMAGE_KIND_BASE=7000,ACTION_POTENCE_BASE=8000;
 private static final int RADIUS_DATA_BASE=SpellPresentation.PLAN_DATA_SIZE;
 private static final int DAMAGE_KIND_DATA_BASE=RADIUS_DATA_BASE+SpellPresentation.CELLS;
 private static final int POTENCE_DATA_BASE=DAMAGE_KIND_DATA_BASE+SpellPresentation.CELLS;
 private final Container paper=new SimpleContainer(1);
 private final ContainerData spellData=new SimpleContainerData(POTENCE_DATA_BASE+SpellPresentation.CELLS);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);
  addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
  int sx=80,sy=204;
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
  for(int cell=0;cell<SpellPresentation.CELLS;cell++){
   spellData.set(RADIUS_DATA_BASE+cell,SpellPresentation.RADIUS_DEFAULT);
   spellData.set(DAMAGE_KIND_DATA_BASE+cell,SpellPresentation.DAMAGE_KIND_DEFAULT);
   spellData.set(POTENCE_DATA_BASE+cell,SpellPresentation.POTENCE_DEFAULT);
  }
  addDataSlots(spellData);
 }
 private int off(int cell){return cell*SpellPresentation.STRIDE;}
 public int typeAt(int cell){return valid(cell)?spellData.get(off(cell)):SpellPresentation.TYPE_EMPTY;}
 public int styleAt(int cell){return valid(cell)?spellData.get(off(cell)+1):SpellPresentation.STYLE_DEFAULT;}
 public int visualAt(int cell){return valid(cell)?spellData.get(off(cell)+2):SpellPresentation.VISUAL_DEFAULT;}
 public int radiusAt(int cell){return valid(cell)?Mth.clamp(spellData.get(RADIUS_DATA_BASE+cell),SpellPresentation.RADIUS_MIN,SpellPresentation.RADIUS_MAX):SpellPresentation.RADIUS_DEFAULT;}
 public int damageKindAt(int cell){return valid(cell)?Mth.clamp(spellData.get(DAMAGE_KIND_DATA_BASE+cell),0,SpellPresentation.DAMAGE_KIND_COUNT-1):SpellPresentation.DAMAGE_KIND_DEFAULT;}
 public int potenceAt(int cell){return valid(cell)?Mth.clamp(spellData.get(POTENCE_DATA_BASE+cell),SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX):SpellPresentation.POTENCE_DEFAULT;}
 public boolean hasAny(){for(int i=0;i<SpellPresentation.CELLS;i++)if(typeAt(i)!=SpellPresentation.TYPE_EMPTY)return true;return false;}
 public int[] snapshotPlan(){int[] values=SpellPresentation.emptyPlan();for(int i=0;i<values.length;i++)values[i]=spellData.get(i);return values;}
 public int[] snapshotRadii(){int[] values=SpellPresentation.emptyRadii();for(int i=0;i<values.length;i++)values[i]=radiusAt(i);return values;}
 public int[] snapshotDamageKinds(){int[] values=SpellPresentation.emptyDamageKinds();for(int i=0;i<values.length;i++)values[i]=damageKindAt(i);return values;}
 public int[] snapshotPotences(){int[] values=SpellPresentation.emptyPotences();for(int i=0;i<values.length;i++)values[i]=potenceAt(i);return values;}
 private boolean valid(int cell){return cell>=0&&cell<SpellPresentation.CELLS;}
 private void setCell(int cell,int type,int style,int visual){if(!valid(cell))return;int o=off(cell);spellData.set(o,type);spellData.set(o+1,style);spellData.set(o+2,visual);spellData.set(RADIUS_DATA_BASE+cell,SpellPresentation.RADIUS_DEFAULT);spellData.set(DAMAGE_KIND_DATA_BASE+cell,SpellPresentation.DAMAGE_KIND_DEFAULT);spellData.set(POTENCE_DATA_BASE+cell,SpellPresentation.POTENCE_DEFAULT);}
 private void swap(int a,int b){
  if(!valid(a)||!valid(b)||a==b)return;
  for(int k=0;k<SpellPresentation.STRIDE;k++){int oa=off(a)+k,ob=off(b)+k,v=spellData.get(oa);spellData.set(oa,spellData.get(ob));spellData.set(ob,v);}
  swapData(RADIUS_DATA_BASE,a,b);swapData(DAMAGE_KIND_DATA_BASE,a,b);swapData(POTENCE_DATA_BASE,a,b);
 }
 private void swapData(int base,int a,int b){int v=spellData.get(base+a);spellData.set(base+a,spellData.get(base+b));spellData.set(base+b,v);}
 @Override public boolean clickMenuButton(Player player,int id){
  if(id>=ACTION_ADD_MISSILE_BASE&&id<ACTION_ADD_MISSILE_BASE+SpellPresentation.CELLS){setCell(id-ACTION_ADD_MISSILE_BASE,SpellPresentation.TYPE_MISSILE,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT);return true;}
  if(id>=ACTION_ADD_SPHERE_BASE&&id<ACTION_ADD_SPHERE_BASE+SpellPresentation.CELLS){setCell(id-ACTION_ADD_SPHERE_BASE,SpellPresentation.TYPE_SPHERE,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT);return true;}
  if(id>=ACTION_ADD_DAMAGE_BASE&&id<ACTION_ADD_DAMAGE_BASE+SpellPresentation.CELLS){setCell(id-ACTION_ADD_DAMAGE_BASE,SpellPresentation.TYPE_DAMAGE,0,0);return true;}
  if(id>=ACTION_MOVE_BASE&&id<ACTION_MOVE_BASE+SpellPresentation.CELLS*SpellPresentation.CELLS){int code=id-ACTION_MOVE_BASE,from=code/SpellPresentation.CELLS,to=code%SpellPresentation.CELLS;swap(from,to);return true;}
  if(id>=ACTION_CLEAR_BASE&&id<ACTION_CLEAR_BASE+SpellPresentation.CELLS){setCell(id-ACTION_CLEAR_BASE,SpellPresentation.TYPE_EMPTY,0,0);return true;}
  if(id>=ACTION_STYLE_BASE&&id<ACTION_STYLE_BASE+SpellPresentation.CELLS*SpellPresentation.STYLE_COUNT){int code=id-ACTION_STYLE_BASE,cell=code/SpellPresentation.STYLE_COUNT,value=code%SpellPresentation.STYLE_COUNT;if(SpellPresentation.isShape(typeAt(cell)))spellData.set(off(cell)+1,value);return true;}
  if(id>=ACTION_VISUAL_BASE&&id<ACTION_VISUAL_BASE+SpellPresentation.CELLS*SpellPresentation.VISUAL_COUNT){int code=id-ACTION_VISUAL_BASE,cell=code/SpellPresentation.VISUAL_COUNT,value=code%SpellPresentation.VISUAL_COUNT;if(SpellPresentation.isShape(typeAt(cell)))spellData.set(off(cell)+2,value);return true;}
  if(id>=ACTION_RADIUS_BASE&&id<ACTION_RADIUS_BASE+SpellPresentation.CELLS*SpellPresentation.RADIUS_COUNT){int code=id-ACTION_RADIUS_BASE,cell=code/SpellPresentation.RADIUS_COUNT,value=SpellPresentation.RADIUS_MIN+(code%SpellPresentation.RADIUS_COUNT);if(typeAt(cell)==SpellPresentation.TYPE_SPHERE)spellData.set(RADIUS_DATA_BASE+cell,value);return true;}
  if(id>=ACTION_DAMAGE_KIND_BASE&&id<ACTION_DAMAGE_KIND_BASE+SpellPresentation.CELLS*SpellPresentation.DAMAGE_KIND_COUNT){int code=id-ACTION_DAMAGE_KIND_BASE,cell=code/SpellPresentation.DAMAGE_KIND_COUNT,value=code%SpellPresentation.DAMAGE_KIND_COUNT;if(typeAt(cell)==SpellPresentation.TYPE_DAMAGE)spellData.set(DAMAGE_KIND_DATA_BASE+cell,value);return true;}
  if(id>=ACTION_POTENCE_BASE&&id<ACTION_POTENCE_BASE+SpellPresentation.CELLS*SpellPresentation.POTENCE_COUNT){int code=id-ACTION_POTENCE_BASE,cell=code/SpellPresentation.POTENCE_COUNT,value=SpellPresentation.POTENCE_MIN+(code%SpellPresentation.POTENCE_COUNT);if(typeAt(cell)==SpellPresentation.TYPE_DAMAGE)spellData.set(POTENCE_DATA_BASE+cell,value);return true;}
  if(id==ACTION_WRITE){
   if(!hasAny())return false;
   if(!player.getAbilities().instabuild){
    ItemStack paperStack=paper.getItem(0);
    if(!paperStack.is(Items.PAPER)||paperStack.isEmpty())return false;
    paperStack.shrink(1);paper.setChanged();
   }
   ItemStack written=WrittenSpellItem.create(snapshotPlan(),snapshotRadii(),snapshotDamageKinds(),snapshotPotences());
   if(!player.addItem(written))player.drop(written,false);
   return true;
  }
  return false;
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
