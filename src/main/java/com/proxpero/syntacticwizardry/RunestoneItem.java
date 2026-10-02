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
    private static int settingIndex(int cell,SpellPropertyKey key){return cell*SpellPropertyKey.SETTING_COUNT+key.settingIndex();}
    public static int[] emptySettings(){
        int[] out=new int[MAX_CELLS*SpellPropertyKey.SETTING_COUNT];
        for(int cell=0;cell<MAX_CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())out[settingIndex(cell,key)]=SpellPresentation.settingDefault(key);
        return out;
    }
    public static int[] cells(ItemStack stack){
        int[] out=new int[MAX_CELLS];
        int[] raw=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_runestone_cells");
        System.arraycopy(raw,0,out,0,Math.min(raw.length,out.length));
        return out;
    }
    public static int[] settings(ItemStack stack){
        int[] out=emptySettings();
        int[] raw=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_runestone_settings");
        System.arraycopy(raw,0,out,0,Math.min(raw.length,out.length));
        for(int cell=0;cell<MAX_CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())out[settingIndex(cell,key)]=SpellPresentation.clampSetting(key,out[settingIndex(cell,key)]);
        return out;
    }
    public static int settingAt(int[] settings,int cell,SpellPropertyKey key){
        if(settings==null||cell<0||cell>=MAX_CELLS||key==null||!key.isSetting())return key==null?0:SpellPresentation.settingDefault(key);
        int index=settingIndex(cell,key);
        return index<settings.length?SpellPresentation.clampSetting(key,settings[index]):SpellPresentation.settingDefault(key);
    }
    public static int settingAt(ItemStack stack,int cell,SpellPropertyKey key){return settingAt(settings(stack),cell,key);}
    public static void setSetting(int[] settings,int cell,SpellPropertyKey key,int value){
        if(settings==null||cell<0||cell>=MAX_CELLS||key==null||!key.isSetting())return;
        int index=settingIndex(cell,key);if(index<settings.length)settings[index]=SpellPresentation.clampSetting(key,value);
    }
    public static void carve(ItemStack stack,int[] source){carve(stack,source,null);}
    public static void carve(ItemStack stack,int[] source,int[] sourceSettings){
        int[] cells=new int[MAX_CELLS];
        if(source!=null)System.arraycopy(source,0,cells,0,Math.min(source.length,cells.length));
        int[] settings=emptySettings();
        if(sourceSettings!=null)System.arraycopy(sourceSettings,0,settings,0,Math.min(sourceSettings.length,settings.length));
        for(int cell=0;cell<MAX_CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())settings[settingIndex(cell,key)]=SpellPresentation.clampSetting(key,settings[settingIndex(cell,key)]);
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putBoolean("sw_runestone_carved",true);tag.putIntArray("sw_runestone_cells",cells);tag.putIntArray("sw_runestone_settings",settings);});
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> tooltip,TooltipFlag flag){
        super.appendHoverText(stack,context,tooltip,flag);
        tooltip.add(Component.literal("Slots: "+slots));
        tooltip.add(Component.literal(isCarved(stack)?"Carved":"Uncarved"));
    }
}
