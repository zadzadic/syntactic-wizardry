package com.proxpero.syntacticwizardry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CarvingStationMenu extends AbstractContainerMenu {
    public static final int WIDTH=320,HEIGHT=232,MAX_CELLS=15;
    public static final int ACTION_ADD_BASE=100,ACTION_CLEAR_BASE=2000,ACTION_CARVE=3000,ACTION_FORM=3001,ACTION_MOVE_BASE=4000;
    private static final int ACTION_STRIDE=64;
    private final Container station=new SimpleContainer(4);
    private final ContainerData data=new SimpleContainerData(MAX_CELLS);

    public CarvingStationMenu(int id,Inventory inv){
        super(SyntacticWizardry.CARVING_STATION_MENU.get(),id);
        addSlot(new Slot(station,0,20,25){@Override public boolean mayPlace(ItemStack s){return RunestoneItem.isRunestone(s);}});
        addSlot(new Slot(station,1,20,61){@Override public boolean mayPlace(ItemStack s){return isFormIngredient(s);}});
        addSlot(new Slot(station,2,44,61){@Override public boolean mayPlace(ItemStack s){return isFormIngredient(s);}});
        addSlot(new Slot(station,3,68,61){@Override public boolean mayPlace(ItemStack s){return isFormIngredient(s);}});
        int sx=80,sy=204;for(int c=0;c<9;c++)addSlot(new Slot(inv,c,sx+c*18,sy));
        addDataSlots(data);
    }

    public static int actionAdd(int cell,int type){return ACTION_ADD_BASE+cell*ACTION_STRIDE+type;}
    public static int actionClear(int cell){return ACTION_CLEAR_BASE+cell;}
    public static int actionMove(int from,int to){return ACTION_MOVE_BASE+from*MAX_CELLS+to;}
    public ItemStack runestone(){return station.getItem(0);}
    public int faces(){return RunestoneItem.faces(runestone());}
    public boolean carved(){return RunestoneItem.isRunestone(runestone())&&RunestoneItem.isCarved(runestone());}
    public int typeAt(int cell){return cell>=0&&cell<MAX_CELLS?data.get(cell):0;}
    public int activeCells(){return faces()*3;}
    private boolean validCell(int cell){return cell>=0&&cell<activeCells();}
    private boolean editable(){return RunestoneItem.isRunestone(runestone())&&!carved();}
    private boolean allowedType(int type){
        SpellComponentDefinition definition=SpellComponents.byType(type);
        return definition!=null&&!SpellComponents.isModifier(definition);
    }
    private boolean hasAny(){for(int i=0;i<activeCells();i++)if(data.get(i)!=0)return true;return false;}

    @Override public boolean clickMenuButton(Player player,int id){
        if(id>=ACTION_ADD_BASE&&id<ACTION_CLEAR_BASE){
            int code=id-ACTION_ADD_BASE,cell=code/ACTION_STRIDE,type=code%ACTION_STRIDE;
            if(editable()&&validCell(cell)&&allowedType(type)){data.set(cell,type);return true;}
            return false;
        }
        if(id>=ACTION_CLEAR_BASE&&id<ACTION_CARVE){
            int cell=id-ACTION_CLEAR_BASE;
            if(editable()&&validCell(cell)){data.set(cell,0);return true;}
            return false;
        }
        if(id>=ACTION_MOVE_BASE&&id<ACTION_MOVE_BASE+MAX_CELLS*MAX_CELLS){
            int code=id-ACTION_MOVE_BASE,from=code/MAX_CELLS,to=code%MAX_CELLS;
            if(editable()&&validCell(from)&&validCell(to)&&from!=to){
                int moving=data.get(from);
                if(moving==0)return false;
                int displaced=data.get(to);
                data.set(to,moving);
                data.set(from,displaced);
                return true;
            }
            return false;
        }
        if(id==ACTION_CARVE){
            if(!editable()||!hasAny())return false;
            int[] cells=new int[MAX_CELLS];for(int i=0;i<MAX_CELLS;i++)cells[i]=data.get(i);
            RunestoneItem.carve(runestone(),cells);station.setChanged();return true;
        }
        if(id==ACTION_FORM)return formRunestone();
        return false;
    }

    private static boolean isFormIngredient(ItemStack stack){
        return stack.is(Items.RED_DYE)
                || stack.is(Items.IRON_INGOT)
                || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.DIAMOND)
                || stack.is(Items.IRON_PICKAXE)
                || stack.is(Items.GOLDEN_PICKAXE)
                || stack.is(Items.DIAMOND_PICKAXE);
    }

    private boolean formRunestone(){
        if(!station.getItem(0).isEmpty())return false;

        ItemStack material=ItemStack.EMPTY,pick=ItemStack.EMPTY,dye=ItemStack.EMPTY;
        for(int i=1;i<=3;i++){
            ItemStack stack=station.getItem(i);
            if(stack.isEmpty())continue;
            if(stack.is(Items.RED_DYE)){
                if(!dye.isEmpty())return false;
                dye=stack;
            }else if(stack.is(Items.IRON_INGOT)||stack.is(Items.GOLD_INGOT)||stack.is(Items.DIAMOND)){
                if(!material.isEmpty())return false;
                material=stack;
            }else if(stack.is(Items.IRON_PICKAXE)||stack.is(Items.GOLDEN_PICKAXE)||stack.is(Items.DIAMOND_PICKAXE)){
                if(!pick.isEmpty())return false;
                pick=stack;
            }else return false;
        }

        if(material.isEmpty()||pick.isEmpty()||dye.isEmpty())return false;

        Item result=null;
        if(material.is(Items.IRON_INGOT)&&pick.is(Items.IRON_PICKAXE))result=SyntacticWizardry.IRON_RUNESTONE.get();
        else if(material.is(Items.GOLD_INGOT)&&pick.is(Items.GOLDEN_PICKAXE))result=SyntacticWizardry.GOLD_RUNESTONE.get();
        else if(material.is(Items.DIAMOND)&&pick.is(Items.DIAMOND_PICKAXE))result=SyntacticWizardry.DIAMOND_RUNESTONE.get();
        if(result==null)return false;

        material.shrink(1);
        dye.shrink(1);
        station.setItem(0,new ItemStack(result));
        station.setChanged();
        return true;
    }

    @Override public boolean stillValid(Player p){return true;}
    @Override public ItemStack quickMoveStack(Player p,int index){return ItemStack.EMPTY;}
    @Override public void removed(Player player){
        super.removed(player);
        if(!player.level().isClientSide){
            for(int i=0;i<4;i++){
                ItemStack stack=station.removeItemNoUpdate(i);
                if(!stack.isEmpty()&&!player.addItem(stack))player.drop(stack,false);
            }
        }
    }
}
