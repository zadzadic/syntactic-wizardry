package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
public final class SpellPresentation {
 public static final int STYLE_DEFAULT=0,STYLE_ARC=1,STYLE_SPIRAL=2;
 public static final int VISUAL_DEFAULT=0,VISUAL_LARGE_CHUNK=1,VISUAL_SWORD=2,VISUAL_AXE=3,VISUAL_TRIDENT=4,VISUAL_FLAMES=5;
 private SpellPresentation(){}
 public static String styleName(int id){return switch(Mth.clamp(id,0,2)){case STYLE_ARC->"Arc";case STYLE_SPIRAL->"Spiral";default->"Default";};}
 public static String visualName(int id){return switch(Mth.clamp(id,0,5)){case VISUAL_LARGE_CHUNK->"Large Chunk";case VISUAL_SWORD->"Sword";case VISUAL_AXE->"Axe";case VISUAL_TRIDENT->"Trident";case VISUAL_FLAMES->"Flames";default->"Default";};}
 public static ItemStack visualStack(int id){
  return switch(Mth.clamp(id,0,5)){
   case VISUAL_LARGE_CHUNK->new ItemStack(Items.STONE);
   case VISUAL_SWORD->new ItemStack(Items.IRON_SWORD);
   case VISUAL_AXE->new ItemStack(Items.IRON_AXE);
   case VISUAL_TRIDENT->new ItemStack(Items.TRIDENT);
   case VISUAL_FLAMES->new ItemStack(Items.FIRE_CHARGE);
   default->new ItemStack(Items.SNOWBALL);
  };
 }
 public static void writeState(ItemStack stack,int style,int visual){
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putInt("sw_style",Mth.clamp(style,0,2));tag.putInt("sw_visual",Mth.clamp(visual,0,5));});
 }
 public static int readStyle(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return Mth.clamp(tag.getInt("sw_style"),0,2);}
 public static int readVisual(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return Mth.clamp(tag.getInt("sw_visual"),0,5);}
 public static ItemStack projectileStack(int style,int visual){ItemStack stack=visualStack(visual);writeState(stack,style,visual);return stack;}
}
