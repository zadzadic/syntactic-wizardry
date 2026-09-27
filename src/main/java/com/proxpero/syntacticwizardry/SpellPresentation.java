package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
public final class SpellPresentation {
 public static final int ROWS=5,COLS=5,CELLS=ROWS*COLS,STRIDE=3,PLAN_DATA_SIZE=CELLS*STRIDE;
 public static final int TYPE_EMPTY=0,TYPE_MISSILE=1,TYPE_SPHERE=2,TYPE_BOX=3,TYPE_DAMAGE=4,TYPE_TARGET=5,TYPE_CONE=6;
 public static final int STYLE_DEFAULT=0,STYLE_ARC=1,STYLE_SPIRAL=2,STYLE_INNER=3,STYLE_OUTER=4,STYLE_COUNT=5;
 public static final int VISUAL_DEFAULT=0,VISUAL_LARGE_CHUNK=1,VISUAL_SWORD=2,VISUAL_AXE=3,VISUAL_TRIDENT=4,VISUAL_FLAMES=5,VISUAL_COUNT=6;
 public static final int RADIUS_MIN=1,RADIUS_MAX=10,RADIUS_DEFAULT=1,RADIUS_COUNT=RADIUS_MAX-RADIUS_MIN+1;
 public static final int DIMENSION_MIN=1,DIMENSION_MAX=10,DIMENSION_DEFAULT=1;
 public static final int BOX_SIZE_MIN=DIMENSION_MIN,BOX_SIZE_MAX=DIMENSION_MAX,BOX_SIZE_DEFAULT=DIMENSION_DEFAULT;
 public static final int DAMAGE_FIRE=0,DAMAGE_FROST=1,DAMAGE_FORCE=2,DAMAGE_PHYSICAL=3,DAMAGE_ARCANE=4,DAMAGE_ENTROPIC=5,DAMAGE_HOLY=6,DAMAGE_KIND_COUNT=7,DAMAGE_KIND_DEFAULT=DAMAGE_ARCANE;
 public static final int POTENCE_MIN=1,POTENCE_MAX=10,POTENCE_DEFAULT=1,POTENCE_COUNT=POTENCE_MAX-POTENCE_MIN+1;
 public static final int TARGET_BLOCKS=0,TARGET_ENTITIES=1,TARGET_ALL=2,TARGET_TYPE_COUNT=3,TARGET_TYPE_DEFAULT=TARGET_ALL;
 public static final int SETTINGS_DATA_SIZE=CELLS*SpellPropertyKey.SETTING_COUNT;
 private SpellPresentation(){}
 private static int off(int cell){return cell*STRIDE;}
 private static int settingOff(int cell,SpellPropertyKey key){return cell*SpellPropertyKey.SETTING_COUNT+key.settingIndex();}
 public static int[] emptyPlan(){return new int[PLAN_DATA_SIZE];}
 public static int[] emptySettings(){
  int[] values=new int[SETTINGS_DATA_SIZE];
  for(int cell=0;cell<CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())values[settingOff(cell,key)]=settingDefault(key);
  return values;
 }
 public static int typeAt(int[] plan,int cell){return valid(plan,cell)?plan[off(cell)]:TYPE_EMPTY;}
 public static int styleAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+1],0,STYLE_COUNT-1):STYLE_DEFAULT;}
 public static int visualAt(int[] plan,int cell){return valid(plan,cell)?Mth.clamp(plan[off(cell)+2],0,VISUAL_COUNT-1):VISUAL_DEFAULT;}
 public static int settingAt(int[] settings,int cell,SpellPropertyKey key){
  if(!key.isSetting()||settings==null||settings.length<SETTINGS_DATA_SIZE||cell<0||cell>=CELLS)return settingDefault(key);
  return clampSetting(key,settings[settingOff(cell,key)]);
 }
 public static void setSetting(int[] settings,int cell,SpellPropertyKey key,int value){if(key.isSetting()&&settings!=null&&settings.length>=SETTINGS_DATA_SIZE&&cell>=0&&cell<CELLS)settings[settingOff(cell,key)]=clampSetting(key,value);}
 public static int settingDefault(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_DEFAULT;case DAMAGE_KIND->DAMAGE_KIND_DEFAULT;case POTENCE->POTENCE_DEFAULT;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_DEFAULT;case TARGET_TYPE->TARGET_TYPE_DEFAULT;default->0;};}
 public static int settingMin(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_MIN;case DAMAGE_KIND->0;case POTENCE->POTENCE_MIN;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_MIN;case TARGET_TYPE->0;default->0;};}
 public static int settingMax(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_MAX;case DAMAGE_KIND->DAMAGE_KIND_COUNT-1;case POTENCE->POTENCE_MAX;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_MAX;case TARGET_TYPE->TARGET_TYPE_COUNT-1;default->0;};}
 public static int clampSetting(SpellPropertyKey key,int value){return Mth.clamp(value,settingMin(key),settingMax(key));}
 public static int radiusAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.RADIUS);}
 public static int damageKindAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DAMAGE_KIND);}
 public static int potenceAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.POTENCE);}
 public static int boxWidthAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.WIDTH);}
 public static int boxHeightAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.HEIGHT);}
 public static int boxDepthAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DEPTH);}
 public static int targetTypeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.TARGET_TYPE);}
 public static void setCell(int[] plan,int cell,int type,int style,int visual){if(!valid(plan,cell))return;int o=off(cell);plan[o]=type;plan[o+1]=Mth.clamp(style,0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(visual,0,VISUAL_COUNT-1);}
 private static boolean valid(int[] plan,int cell){return plan!=null&&plan.length>=PLAN_DATA_SIZE&&cell>=0&&cell<CELLS;}
 public static boolean rowHasComponents(int[] plan,int row){if(row<0||row>=ROWS)return false;for(int c=0;c<COLS;c++)if(typeAt(plan,row*COLS+c)!=TYPE_EMPTY)return true;return false;}
 public static int firstOccupiedRow(int[] plan){for(int r=0;r<ROWS;r++)if(rowHasComponents(plan,r))return r;return -1;}
 public static boolean isShape(int type){SpellComponentDefinition definition=SpellComponents.byType(type);return definition!=null&&definition.isShape();}
 public static String shapeName(int type){return componentName(type);}
 public static String componentName(int type){SpellComponentDefinition definition=SpellComponents.byType(type);return definition!=null?definition.displayName():"Empty";}
 public static String styleName(int id){return switch(Mth.clamp(id,0,STYLE_COUNT-1)){case STYLE_ARC->"Arc";case STYLE_SPIRAL->"Spiral";case STYLE_INNER->"Inner";case STYLE_OUTER->"Outer";default->"Default";};}
 public static String visualName(int id){return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){case VISUAL_LARGE_CHUNK->"Large Chunk";case VISUAL_SWORD->"Sword";case VISUAL_AXE->"Axe";case VISUAL_TRIDENT->"Trident";case VISUAL_FLAMES->"Flames";default->"Default";};}
 public static String damageKindName(int id){return switch(Mth.clamp(id,0,DAMAGE_KIND_COUNT-1)){case DAMAGE_FIRE->"Fire";case DAMAGE_FROST->"Frost";case DAMAGE_FORCE->"Force";case DAMAGE_PHYSICAL->"Physical";case DAMAGE_ENTROPIC->"Entropic";case DAMAGE_HOLY->"Holy";default->"Arcane";};}
 public static String targetTypeName(int id){return switch(Mth.clamp(id,0,TARGET_TYPE_COUNT-1)){case TARGET_BLOCKS->"Blocks";case TARGET_ENTITIES->"Entities";default->"All";};}
 public static ItemStack visualStack(int id){return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){case VISUAL_LARGE_CHUNK->new ItemStack(Items.STONE);case VISUAL_SWORD->new ItemStack(Items.IRON_SWORD);case VISUAL_AXE->new ItemStack(Items.IRON_AXE);case VISUAL_TRIDENT->new ItemStack(Items.TRIDENT);case VISUAL_FLAMES->new ItemStack(Items.FIRE_CHARGE);default->new ItemStack(Items.SNOWBALL);};}
 public static ItemStack sphereVisualStack(int style,int visual,int radius){ItemStack stack=visualStack(visual);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","sphere_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_sphere_radius",Mth.clamp(radius,RADIUS_MIN,RADIUS_MAX));});return stack;}
 public static ItemStack boxVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction){ItemStack stack=visualStack(visual);net.minecraft.world.phys.Vec3 dir=SpellExecutor.normalizeYaw(direction);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","box_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_box_width",Mth.clamp(width,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_height",Mth.clamp(height,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_depth",Mth.clamp(depth,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putDouble("sw_box_dir_x",dir.x);tag.putDouble("sw_box_dir_z",dir.z);});return stack;}
 public static ItemStack coneVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction){ItemStack stack=visualStack(visual);net.minecraft.world.phys.Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","cone_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_cone_width",Mth.clamp(width,DIMENSION_MIN,DIMENSION_MAX));tag.putInt("sw_cone_height",Mth.clamp(height,DIMENSION_MIN,DIMENSION_MAX));tag.putInt("sw_cone_depth",Mth.clamp(depth,DIMENSION_MIN,DIMENSION_MAX));tag.putDouble("sw_cone_dir_x",dir.x);tag.putDouble("sw_cone_dir_y",dir.y);tag.putDouble("sw_cone_dir_z",dir.z);});return stack;}
 public static int readSphereVisualRadius(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_sphere_radius"),RADIUS_MIN,RADIUS_MAX);}
 public static int readBoxVisualWidth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_width"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualHeight(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_height"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualDepth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_depth"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static net.minecraft.world.phys.Vec3 readBoxVisualDirection(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return SpellExecutor.normalizeYaw(new net.minecraft.world.phys.Vec3(tag.getDouble("sw_box_dir_x"),0.0,tag.getDouble("sw_box_dir_z")));}
 public static int readConeVisualWidth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_width"),DIMENSION_MIN,DIMENSION_MAX);}
 public static int readConeVisualHeight(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_height"),DIMENSION_MIN,DIMENSION_MAX);}
 public static int readConeVisualDepth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_depth"),DIMENSION_MIN,DIMENSION_MAX);}
 public static net.minecraft.world.phys.Vec3 readConeVisualDirection(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();net.minecraft.world.phys.Vec3 dir=new net.minecraft.world.phys.Vec3(tag.getDouble("sw_cone_dir_x"),tag.getDouble("sw_cone_dir_y"),tag.getDouble("sw_cone_dir_z"));return dir.lengthSqr()>1.0E-8?dir.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);}
 public static void writePlan(ItemStack stack,int[] source,int[] settingSource){int[] plan=normalizePlan(source),settings=normalizeSettings(settingSource);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","spell");tag.putIntArray("sw_plan",plan);tag.putIntArray("sw_settings",settings);});}
 public static int[] readPlan(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int[] raw=tag.getIntArray("sw_plan");if(raw.length>0)return normalizePlan(raw);int[] legacy=emptyPlan();if("missile".equals(tag.getString("sw_shape")))setCell(legacy,0,TYPE_MISSILE,tag.getInt("sw_style"),tag.getInt("sw_visual"));return legacy;}
 public static int[] readSettings(ItemStack stack){
  CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  int[] raw=tag.getIntArray("sw_settings");
  if(raw.length>0)return normalizeSettings(raw);
  int[] settings=emptySettings();
  importLegacy(settings,tag.getIntArray("sw_radius"),SpellPropertyKey.RADIUS);
  importLegacy(settings,tag.getIntArray("sw_damage_kind"),SpellPropertyKey.DAMAGE_KIND);
  importLegacy(settings,tag.getIntArray("sw_potence"),SpellPropertyKey.POTENCE);
  importLegacy(settings,tag.getIntArray("sw_box_width"),SpellPropertyKey.WIDTH);
  importLegacy(settings,tag.getIntArray("sw_box_height"),SpellPropertyKey.HEIGHT);
  importLegacy(settings,tag.getIntArray("sw_box_depth"),SpellPropertyKey.DEPTH);
  return settings;
 }
 private static void importLegacy(int[] settings,int[] legacy,SpellPropertyKey key){if(legacy==null||legacy.length==0)return;for(int cell=0;cell<Math.min(CELLS,legacy.length);cell++)setSetting(settings,cell,key,legacy[cell]);}
 private static int[] normalizePlan(int[] source){int[] plan=emptyPlan();if(source!=null)System.arraycopy(source,0,plan,0,Math.min(source.length,plan.length));for(int i=0;i<CELLS;i++){int o=off(i);if(plan[o]!=TYPE_EMPTY&&SpellComponents.byType(plan[o])==null)plan[o]=TYPE_EMPTY;plan[o+1]=Mth.clamp(plan[o+1],0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(plan[o+2],0,VISUAL_COUNT-1);}return plan;}
 private static int[] normalizeSettings(int[] source){int[] values=emptySettings();if(source!=null)System.arraycopy(source,0,values,0,Math.min(source.length,values.length));for(int cell=0;cell<CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())values[settingOff(cell,key)]=clampSetting(key,values[settingOff(cell,key)]);return values;}
 public static ItemStack projectileStack(int[] plan,int[] settings,int row,int cell,net.minecraft.world.phys.Vec3 castYaw){ItemStack stack=visualStack(visualAt(plan,cell));int[] planCopy=normalizePlan(plan),settingsCopy=normalizeSettings(settings);net.minecraft.world.phys.Vec3 yaw=SpellExecutor.normalizeYaw(castYaw);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putIntArray("sw_plan",planCopy);tag.putIntArray("sw_settings",settingsCopy);tag.putInt("sw_row",row);tag.putInt("sw_cell",cell);tag.putInt("sw_style",styleAt(planCopy,cell));tag.putInt("sw_visual",visualAt(planCopy,cell));tag.putDouble("sw_cast_yaw_x",yaw.x);tag.putDouble("sw_cast_yaw_z",yaw.z);});return stack;}
 public static int readRow(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_row");}
 public static int readCell(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cell");}
 public static int readStyle(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_style"),0,STYLE_COUNT-1);}
 public static int readVisual(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_visual"),0,VISUAL_COUNT-1);}
 public static net.minecraft.world.phys.Vec3 readCastYaw(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return SpellExecutor.normalizeYaw(new net.minecraft.world.phys.Vec3(tag.getDouble("sw_cast_yaw_x"),0.0,tag.getDouble("sw_cast_yaw_z")));}
}
