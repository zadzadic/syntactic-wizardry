package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import java.util.Arrays;
public final class SpellPresentation {
 public static final int ROWS=5,COLS=5,CELLS=ROWS*COLS,STRIDE=3;
 public static final int TYPE_EMPTY=0,TYPE_MISSILE=1;
 public static final int STYLE_DEFAULT=0,STYLE_ARC=1,STYLE_SPIRAL=2,STYLE_COUNT=3;
 public static final int VISUAL_DEFAULT=0,VISUAL_LARGE_CHUNK=1,VISUAL_SWORD=2,VISUAL_AXE=3,VISUAL_TRIDENT=4,VISUAL_FLAMES=5,VISUAL_COUNT=6;
 private SpellPresentation(){}
 private static int off(int cell){return cell*STRIDE;}
 public static int[] emptyPlan(){return new int[CELLS*STRIDE];}
 public static int typeAt(int[] plan,int cell){return valid(plan,cell)?plan[off(cell)]:TYPE_EMPTY;}
 public static int styleAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+1],0,STYLE_COUNT-1):STYLE_DEFAULT;}
 public static int visualAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+2],0,VISUAL_COUNT-1):VISUAL_DEFAULT;}
 public static void setCell(int[] plan,int cell,int type,int style,int visual){if(!valid(plan,cell))return;int o=off(cell);plan[o]=type;plan[o+1]=Mth.clamp(style,0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(visual,0,VISUAL_COUNT-1);}
 private static boolean valid(int[] plan,int cell){return plan!=null&&plan.length>=CELLS*STRIDE&&cell>=0&&cell<CELLS;}
 public static boolean rowHasComponents(int[] plan,int row){if(row<0||row>=ROWS)return false;for(int c=0;c<COLS;c++)if(typeAt(plan,row*COLS+c)!=TYPE_EMPTY)return true;return false;}
 public static int firstOccupiedRow(int[] plan){for(int r=0;r<ROWS;r++)if(rowHasComponents(plan,r))return r;return -1;}
 public static String styleName(int id){return switch(Mth.clamp(id,0,STYLE_COUNT-1)){case STYLE_ARC->"Arc";case STYLE_SPIRAL->"Spiral";default->"Default";};}
 public static String visualName(int id){return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){case VISUAL_LARGE_CHUNK->"Large Chunk";case VISUAL_SWORD->"Sword";case VISUAL_AXE->"Axe";case VISUAL_TRIDENT->"Trident";case VISUAL_FLAMES->"Flames";default->"Default";};}
 public static ItemStack visualStack(int id){
  return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){
   case VISUAL_LARGE_CHUNK->new ItemStack(Items.STONE);
   case VISUAL_SWORD->new ItemStack(Items.IRON_SWORD);
   case VISUAL_AXE->new ItemStack(Items.IRON_AXE);
   case VISUAL_TRIDENT->new ItemStack(Items.TRIDENT);
   case VISUAL_FLAMES->new ItemStack(Items.FIRE_CHARGE);
   default->new ItemStack(Items.SNOWBALL);
  };
 }
 public static void writePlan(ItemStack stack,int[] source){
  int[] plan=normalize(source);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putIntArray("sw_plan",plan);});
 }
 public static int[] readPlan(ItemStack stack){
  CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  int[] raw=tag.getIntArray("sw_plan");
  if(raw.length>0)return normalize(raw);
  int[] legacy=emptyPlan();
  if("missile".equals(tag.getString("sw_shape"))){setCell(legacy,0,TYPE_MISSILE,tag.getInt("sw_style"),tag.getInt("sw_visual"));}
  return legacy;
 }
 private static int[] normalize(int[] source){
  int[] plan=emptyPlan();
  if(source!=null)System.arraycopy(source,0,plan,0,Math.min(source.length,plan.length));
  for(int i=0;i<CELLS;i++){int o=off(i);if(plan[o]!=TYPE_MISSILE)plan[o]=TYPE_EMPTY;plan[o+1]=Mth.clamp(plan[o+1],0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(plan[o+2],0,VISUAL_COUNT-1);}
  return plan;
 }
 public static ItemStack projectileStack(int[] plan,int row,int cell){
  ItemStack stack=visualStack(visualAt(plan,cell));
  int[] copy=normalize(plan);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putIntArray("sw_plan",copy);tag.putInt("sw_row",row);tag.putInt("sw_cell",cell);tag.putInt("sw_style",styleAt(copy,cell));tag.putInt("sw_visual",visualAt(copy,cell));});
  return stack;
 }
 public static int readRow(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_row");}
 public static int readCell(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cell");}
 public static int readStyle(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_style"),0,STYLE_COUNT-1);}
 public static int readVisual(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_visual"),0,VISUAL_COUNT-1);}
}
