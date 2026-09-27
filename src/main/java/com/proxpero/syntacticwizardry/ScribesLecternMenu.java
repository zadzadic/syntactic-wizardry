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
 public static final int ACTION_ADD_MISSILE=1,ACTION_STYLE_BASE=10,ACTION_VISUAL_BASE=20,ACTION_WRITE=100;
 private final Container paper=new SimpleContainer(1);
 private final ContainerData spellData=new SimpleContainerData(3);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);
  addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(ItemStack s){return s.is(Items.PAPER);}});
  int sx=80,sy=204;
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
  addDataSlots(spellData);
 }
 public boolean hasMissile(){return spellData.get(0)!=0;}
 public int style(){return spellData.get(1);}
 public int visual(){return spellData.get(2);}
 @Override public boolean clickMenuButton(Player player,int id){
  if(id==ACTION_ADD_MISSILE){spellData.set(0,1);return true;}
  if(id>=ACTION_STYLE_BASE&&id<ACTION_STYLE_BASE+3){if(!hasMissile())return false;spellData.set(1,id-ACTION_STYLE_BASE);return true;}
  if(id>=ACTION_VISUAL_BASE&&id<ACTION_VISUAL_BASE+6){if(!hasMissile())return false;spellData.set(2,id-ACTION_VISUAL_BASE);return true;}
  if(id==ACTION_WRITE){
   if(!hasMissile())return false;
   ItemStack paperStack=paper.getItem(0);
   if(!paperStack.is(Items.PAPER)||paperStack.isEmpty())return false;
   paperStack.shrink(1);
   paper.setChanged();
   ItemStack written=WrittenSpellItem.create(style(),visual());
   if(!player.addItem(written))player.drop(written,false);
   return true;
  }
  return false;
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
}
