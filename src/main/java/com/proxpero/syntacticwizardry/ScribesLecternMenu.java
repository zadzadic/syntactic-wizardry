package com.proxpero.syntacticwizardry;
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
 public static final int ACTION_ADD_BASE=100,ACTION_MOVE_BASE=1000,ACTION_CLEAR_BASE=2000,ACTION_STYLE_BASE=3000,ACTION_VISUAL_BASE=4000,ACTION_WRITE=5000;
 private final Container paper=new SimpleContainer(1);
 private final ContainerData spellData=new SimpleContainerData(SpellPresentation.CELLS*SpellPresentation.STRIDE);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);
  addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
  int sx=80,sy=204;
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
  addDataSlots(spellData);
 }
 private int off(int cell){return cell*SpellPresentation.STRIDE;}
 public int typeAt(int cell){return valid(cell)?spellData.get(off(cell)):SpellPresentation.TYPE_EMPTY;}
 public int styleAt(int cell){return valid(cell)?spellData.get(off(cell)+1):SpellPresentation.STYLE_DEFAULT;}
 public int visualAt(int cell){return valid(cell)?spellData.get(off(cell)+2):SpellPresentation.VISUAL_DEFAULT;}
 public boolean hasAny(){for(int i=0;i<SpellPresentation.CELLS;i++)if(typeAt(i)!=SpellPresentation.TYPE_EMPTY)return true;return false;}
 public int[] snapshotPlan(){int[] plan=SpellPresentation.emptyPlan();for(int i=0;i<plan.length;i++)plan[i]=spellData.get(i);return plan;}
 private boolean valid(int cell){return cell>=0&&cell<SpellPresentation.CELLS;}
 private void setCell(int cell,int type,int style,int visual){if(!valid(cell))return;int o=off(cell);spellData.set(o,type);spellData.set(o+1,style);spellData.set(o+2,visual);}
 private void swap(int a,int b){if(!valid(a)||!valid(b)||a==b)return;for(int k=0;k<SpellPresentation.STRIDE;k++){int oa=off(a)+k,ob=off(b)+k,v=spellData.get(oa);spellData.set(oa,spellData.get(ob));spellData.set(ob,v);}}
 @Override public boolean clickMenuButton(Player player,int id){
  if(id>=ACTION_ADD_BASE&&id<ACTION_ADD_BASE+SpellPresentation.CELLS){int cell=id-ACTION_ADD_BASE;setCell(cell,SpellPresentation.TYPE_MISSILE,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT);return true;}
  if(id>=ACTION_MOVE_BASE&&id<ACTION_MOVE_BASE+SpellPresentation.CELLS*SpellPresentation.CELLS){int code=id-ACTION_MOVE_BASE,from=code/SpellPresentation.CELLS,to=code%SpellPresentation.CELLS;swap(from,to);return true;}
  if(id>=ACTION_CLEAR_BASE&&id<ACTION_CLEAR_BASE+SpellPresentation.CELLS){setCell(id-ACTION_CLEAR_BASE,SpellPresentation.TYPE_EMPTY,0,0);return true;}
  if(id>=ACTION_STYLE_BASE&&id<ACTION_STYLE_BASE+SpellPresentation.CELLS*SpellPresentation.STYLE_COUNT){int code=id-ACTION_STYLE_BASE,cell=code/SpellPresentation.STYLE_COUNT,value=code%SpellPresentation.STYLE_COUNT;if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+1,value);return true;}
  if(id>=ACTION_VISUAL_BASE&&id<ACTION_VISUAL_BASE+SpellPresentation.CELLS*SpellPresentation.VISUAL_COUNT){int code=id-ACTION_VISUAL_BASE,cell=code/SpellPresentation.VISUAL_COUNT,value=code%SpellPresentation.VISUAL_COUNT;if(typeAt(cell)!=SpellPresentation.TYPE_EMPTY)spellData.set(off(cell)+2,value);return true;}
  if(id==ACTION_WRITE){
   if(!hasAny())return false;
   ItemStack paperStack=paper.getItem(0);
   if(!paperStack.is(Items.PAPER)||paperStack.isEmpty())return false;
   paperStack.shrink(1);
   paper.setChanged();
   ItemStack written=WrittenSpellItem.create(snapshotPlan());
   if(!player.addItem(written))player.drop(written,false);
   return true;
  }
  return false;
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
