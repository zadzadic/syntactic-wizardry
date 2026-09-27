package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import java.util.Arrays;
public final class SpellPresentation {
 public static final int ROWS=5,COLS=5,CELLS=ROWS*COLS,STRIDE=3,PLAN_DATA_SIZE=CELLS*STRIDE;
 public static final int TYPE_EMPTY=0,TYPE_MISSILE=1,TYPE_SPHERE=2,TYPE_BOX=3,TYPE_DAMAGE=4;
 public static final int STYLE_DEFAULT=0,STYLE_ARC=1,STYLE_SPIRAL=2,STYLE_INNER=3,STYLE_OUTER=4,STYLE_COUNT=5;
 public static final int VISUAL_DEFAULT=0,VISUAL_LARGE_CHUNK=1,VISUAL_SWORD=2,VISUAL_AXE=3,VISUAL_TRIDENT=4,VISUAL_FLAMES=5,VISUAL_COUNT=6;
 public static final int RADIUS_MIN=1,RADIUS_MAX=10,RADIUS_DEFAULT=1,RADIUS_COUNT=RADIUS_MAX-RADIUS_MIN+1;
 public static final int BOX_SIZE_MIN=1,BOX_SIZE_MAX=10,BOX_SIZE_DEFAULT=1;
 public static final int DAMAGE_FIRE=0,DAMAGE_FROST=1,DAMAGE_FORCE=2,DAMAGE_PHYSICAL=3,DAMAGE_ARCANE=4,DAMAGE_ENTROPIC=5,DAMAGE_HOLY=6,DAMAGE_KIND_COUNT=7,DAMAGE_KIND_DEFAULT=DAMAGE_ARCANE;
 public static final int POTENCE_MIN=1,POTENCE_MAX=10,POTENCE_DEFAULT=1,POTENCE_COUNT=POTENCE_MAX-POTENCE_MIN+1;
 private SpellPresentation(){}
 private static int off(int cell){return cell*STRIDE;}
 public static int[] emptyPlan(){return new int[PLAN_DATA_SIZE];}
 public static int[] emptyRadii(){int[] r=new int[CELLS];Arrays.fill(r,RADIUS_DEFAULT);return r;}
 public static int[] emptyDamageKinds(){int[] d=new int[CELLS];Arrays.fill(d,DAMAGE_KIND_DEFAULT);return d;}
 public static int[] emptyPotences(){int[] p=new int[CELLS];Arrays.fill(p,POTENCE_DEFAULT);return p;}
 public static int[] emptyBoxWidths(){int[] v=new int[CELLS];Arrays.fill(v,BOX_SIZE_DEFAULT);return v;}
 public static int[] emptyBoxHeights(){int[] v=new int[CELLS];Arrays.fill(v,BOX_SIZE_DEFAULT);return v;}
 public static int[] emptyBoxDepths(){int[] v=new int[CELLS];Arrays.fill(v,BOX_SIZE_DEFAULT);return v;}
 public static int typeAt(int[] plan,int cell){return valid(plan,cell)?plan[off(cell)]:TYPE_EMPTY;}
 public static int styleAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+1],0,STYLE_COUNT-1):STYLE_DEFAULT;}
 public static int visualAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+2],0,VISUAL_COUNT-1):VISUAL_DEFAULT;}
 public static int radiusAt(int[] radii,int cell){return arrayValue(radii,cell,RADIUS_DEFAULT,RADIUS_MIN,RADIUS_MAX);}
 public static int damageKindAt(int[] kinds,int cell){return arrayValue(kinds,cell,DAMAGE_KIND_DEFAULT,0,DAMAGE_KIND_COUNT-1);}
 public static int potenceAt(int[] potences,int cell){return arrayValue(potences,cell,POTENCE_DEFAULT,POTENCE_MIN,POTENCE_MAX);}
 public static int boxWidthAt(int[] values,int cell){return arrayValue(values,cell,BOX_SIZE_DEFAULT,BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int boxHeightAt(int[] values,int cell){return arrayValue(values,cell,BOX_SIZE_DEFAULT,BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int boxDepthAt(int[] values,int cell){return arrayValue(values,cell,BOX_SIZE_DEFAULT,BOX_SIZE_MIN,BOX_SIZE_MAX);}
 private static int arrayValue(int[] values,int cell,int fallback,int min,int max){return values!=null&&cell>=0&&cell<CELLS&&cell<values.length?Mth.clamp(values[cell],min,max):fallback;}
 public static void setCell(int[] plan,int cell,int type,int style,int visual){if(!valid(plan,cell))return;int o=off(cell);plan[o]=type;plan[o+1]=Mth.clamp(style,0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(visual,0,VISUAL_COUNT-1);}
 public static void setRadius(int[] radii,int cell,int radius){if(radii!=null&&cell>=0&&cell<CELLS&&cell<radii.length)radii[cell]=Mth.clamp(radius,RADIUS_MIN,RADIUS_MAX);}
 private static boolean valid(int[] plan,int cell){return plan!=null&&plan.length>=PLAN_DATA_SIZE&&cell>=0&&cell<CELLS;}
 public static boolean rowHasComponents(int[] plan,int row){if(row<0||row>=ROWS)return false;for(int c=0;c<COLS;c++)if(typeAt(plan,row*COLS+c)!=TYPE_EMPTY)return true;return false;}
 public static int firstOccupiedRow(int[] plan){for(int r=0;r<ROWS;r++)if(rowHasComponents(plan,r))return r;return -1;}
 public static boolean isShape(int type){SpellComponentDefinition definition=SpellComponents.byType(type);return definition!=null&&definition.isShape();}
 public static String shapeName(int type){return componentName(type);}
 public static String componentName(int type){SpellComponentDefinition definition=SpellComponents.byType(type);return definition!=null?definition.displayName():"Empty";}
 public static String styleName(int id){return switch(Mth.clamp(id,0,STYLE_COUNT-1)){case STYLE_ARC->"Arc";case STYLE_SPIRAL->"Spiral";case STYLE_INNER->"Inner";case STYLE_OUTER->"Outer";default->"Default";};}
 public static String visualName(int id){return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){case VISUAL_LARGE_CHUNK->"Large Chunk";case VISUAL_SWORD->"Sword";case VISUAL_AXE->"Axe";case VISUAL_TRIDENT->"Trident";case VISUAL_FLAMES->"Flames";default->"Default";};}
 public static String damageKindName(int id){return switch(Mth.clamp(id,0,DAMAGE_KIND_COUNT-1)){case DAMAGE_FIRE->"Fire";case DAMAGE_FROST->"Frost";case DAMAGE_FORCE->"Force";case DAMAGE_PHYSICAL->"Physical";case DAMAGE_ENTROPIC->"Entropic";case DAMAGE_HOLY->"Holy";default->"Arcane";};}
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
 public static ItemStack sphereVisualStack(int style,int visual,int radius){
  ItemStack stack=visualStack(visual);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","sphere_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_sphere_radius",Mth.clamp(radius,RADIUS_MIN,RADIUS_MAX));});
  return stack;
 }
 public static ItemStack boxVisualStack(int style,int visual,int width,int height,int depth){
  ItemStack stack=visualStack(visual);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","box_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_box_width",Mth.clamp(width,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_height",Mth.clamp(height,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_depth",Mth.clamp(depth,BOX_SIZE_MIN,BOX_SIZE_MAX));});
  return stack;
 }
 public static int readSphereVisualRadius(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_sphere_radius"),RADIUS_MIN,RADIUS_MAX);}
 public static int readBoxVisualWidth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_width"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualHeight(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_height"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualDepth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_depth"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static void writePlan(ItemStack stack,int[] source,int[] radiusSource,int[] damageSource,int[] potenceSource,int[] widthSource,int[] heightSource,int[] depthSource){
  int[] plan=normalizePlan(source);
  int[] radii=normalizeRadii(radiusSource);
  int[] damageKinds=normalizeDamageKinds(damageSource);
  int[] potences=normalizePotences(potenceSource);
  int[] widths=normalizeBoxSizes(widthSource);
  int[] heights=normalizeBoxSizes(heightSource);
  int[] depths=normalizeBoxSizes(depthSource);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","spell");tag.putIntArray("sw_plan",plan);tag.putIntArray("sw_radius",radii);tag.putIntArray("sw_damage_kind",damageKinds);tag.putIntArray("sw_potence",potences);tag.putIntArray("sw_box_width",widths);tag.putIntArray("sw_box_height",heights);tag.putIntArray("sw_box_depth",depths);});
 }
 public static void writePlan(ItemStack stack,int[] source,int[] radiusSource,int[] damageSource,int[] potenceSource){writePlan(stack,source,radiusSource,damageSource,potenceSource,emptyBoxWidths(),emptyBoxHeights(),emptyBoxDepths());}
 public static int[] readPlan(ItemStack stack){
  CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  int[] raw=tag.getIntArray("sw_plan");
  if(raw.length>0)return normalizePlan(raw);
  int[] legacy=emptyPlan();
  if("missile".equals(tag.getString("sw_shape"))){setCell(legacy,0,TYPE_MISSILE,tag.getInt("sw_style"),tag.getInt("sw_visual"));}
  return legacy;
 }
 public static int[] readRadii(ItemStack stack){return normalizeRadii(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_radius"));}
 public static int[] readDamageKinds(ItemStack stack){return normalizeDamageKinds(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_damage_kind"));}
 public static int[] readPotences(ItemStack stack){return normalizePotences(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_potence"));}
 public static int[] readBoxWidths(ItemStack stack){return normalizeBoxSizes(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_box_width"));}
 public static int[] readBoxHeights(ItemStack stack){return normalizeBoxSizes(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_box_height"));}
 public static int[] readBoxDepths(ItemStack stack){return normalizeBoxSizes(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("sw_box_depth"));}
 private static int[] normalizePlan(int[] source){
  int[] plan=emptyPlan();
  if(source!=null)System.arraycopy(source,0,plan,0,Math.min(source.length,plan.length));
  for(int i=0;i<CELLS;i++){
   int o=off(i);
   if(plan[o]!=TYPE_EMPTY&&SpellComponents.byType(plan[o])==null)plan[o]=TYPE_EMPTY;
   plan[o+1]=Mth.clamp(plan[o+1],0,STYLE_COUNT-1);
   plan[o+2]=Mth.clamp(plan[o+2],0,VISUAL_COUNT-1);
  }
  return plan;
 }
 private static int[] normalizeRadii(int[] source){int[] values=emptyRadii();if(source!=null&&source.length>0)System.arraycopy(source,0,values,0,Math.min(source.length,values.length));for(int i=0;i<values.length;i++)values[i]=Mth.clamp(values[i],RADIUS_MIN,RADIUS_MAX);return values;}
 private static int[] normalizeDamageKinds(int[] source){int[] values=emptyDamageKinds();if(source!=null&&source.length>0)System.arraycopy(source,0,values,0,Math.min(source.length,values.length));for(int i=0;i<values.length;i++)values[i]=Mth.clamp(values[i],0,DAMAGE_KIND_COUNT-1);return values;}
 private static int[] normalizePotences(int[] source){int[] values=emptyPotences();if(source!=null&&source.length>0)System.arraycopy(source,0,values,0,Math.min(source.length,values.length));for(int i=0;i<values.length;i++)values[i]=Mth.clamp(values[i],POTENCE_MIN,POTENCE_MAX);return values;}
 private static int[] normalizeBoxSizes(int[] source){int[] values=emptyBoxWidths();if(source!=null&&source.length>0)System.arraycopy(source,0,values,0,Math.min(source.length,values.length));for(int i=0;i<values.length;i++)values[i]=Mth.clamp(values[i],BOX_SIZE_MIN,BOX_SIZE_MAX);return values;}
 public static ItemStack projectileStack(int[] plan,int[] radii,int[] damageKinds,int[] potences,int[] widths,int[] heights,int[] depths,int row,int cell){
  ItemStack stack=visualStack(visualAt(plan,cell));
  int[] planCopy=normalizePlan(plan),radiusCopy=normalizeRadii(radii),damageCopy=normalizeDamageKinds(damageKinds),potenceCopy=normalizePotences(potences),widthCopy=normalizeBoxSizes(widths),heightCopy=normalizeBoxSizes(heights),depthCopy=normalizeBoxSizes(depths);
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putIntArray("sw_plan",planCopy);tag.putIntArray("sw_radius",radiusCopy);tag.putIntArray("sw_damage_kind",damageCopy);tag.putIntArray("sw_potence",potenceCopy);tag.putIntArray("sw_box_width",widthCopy);tag.putIntArray("sw_box_height",heightCopy);tag.putIntArray("sw_box_depth",depthCopy);tag.putInt("sw_row",row);tag.putInt("sw_cell",cell);tag.putInt("sw_style",styleAt(planCopy,cell));tag.putInt("sw_visual",visualAt(planCopy,cell));});
  return stack;
 }
 public static int readRow(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_row");}
 public static int readCell(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cell");}
 public static int readStyle(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_style"),0,STYLE_COUNT-1);}
 public static int readVisual(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_visual"),0,VISUAL_COUNT-1);}
}
