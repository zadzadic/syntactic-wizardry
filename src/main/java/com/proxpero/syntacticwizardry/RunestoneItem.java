package com.proxpero.syntacticwizardry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import java.util.List;

public final class RunestoneItem extends Item {
    public static final int MAX_CELLS=15;
    private final int slots;
    public RunestoneItem(Properties properties,int slots){super(properties);this.slots=Math.max(2,Math.min(5,slots));}
    public int slots(){return slots;}
    public static boolean isRunestone(ItemStack stack){return stack!=null&&!stack.isEmpty()&&stack.getItem() instanceof RunestoneItem;}
    public static int slots(ItemStack stack){return stack!=null&&stack.getItem() instanceof RunestoneItem item?item.slots():0;}
    public static boolean isCarved(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("sw_runestone_carved");}
    public static int[] cells(ItemStack stack){
        int[] out=new int[MAX_CELLS];
        int[] raw=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_runestone_cells");
        System.arraycopy(raw,0,out,0,Math.min(raw.length,out.length));
        return out;
    }
    public static void carve(ItemStack stack,int[] source){
        int[] cells=new int[MAX_CELLS];
        if(source!=null)System.arraycopy(source,0,cells,0,Math.min(source.length,cells.length));
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putBoolean("sw_runestone_carved",true);tag.putIntArray("sw_runestone_cells",cells);});
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> tooltip,TooltipFlag flag){
        super.appendHoverText(stack,context,tooltip,flag);
        tooltip.add(Component.literal("Slots: "+slots));
        tooltip.add(Component.literal(isCarved(stack)?"Carved":"Uncarved"));
    }
}
