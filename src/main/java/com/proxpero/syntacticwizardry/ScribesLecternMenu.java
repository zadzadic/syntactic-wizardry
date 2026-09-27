package com.proxpero.syntacticwizardry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
public final class ScribesLecternMenu extends AbstractContainerMenu {
 public static final int WIDTH=320,HEIGHT=232;
 private final Container paper=new SimpleContainer(1);
 public ScribesLecternMenu(int id,Inventory inv){super(SyntacticWizardry.SCRIBES_LECTERN_MENU.get(),id);
  addSlot(new Slot(paper,0,20,25){@Override public boolean mayPlace(net.minecraft.world.item.ItemStack s){return s.is(Items.PAPER);}});
  int sx=80, sy=204;
  for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
 }
 @Override public boolean stillValid(Player p){return true;}
 @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player p,int index){return net.minecraft.world.item.ItemStack.EMPTY;}
}
