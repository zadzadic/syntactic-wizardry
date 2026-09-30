package com.proxpero.syntacticwizardry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.component.CustomData;
public final class SpellPresentation {
 public static final int ROWS=5,COLS=5,CELLS=ROWS*COLS,STRIDE=3,PLAN_DATA_SIZE=CELLS*STRIDE;
 public static final int TYPE_EMPTY=0,TYPE_MISSILE=1,TYPE_SPHERE=2,TYPE_BOX=3,TYPE_DAMAGE=4,TYPE_TARGET=5,TYPE_CONE=6,TYPE_TOUCH=8,TYPE_DIG=9;
 public static final int STYLE_DEFAULT=0,STYLE_ARC=1,STYLE_SPIRAL=2,STYLE_INNER=3,STYLE_OUTER=4,STYLE_COUNT=5;
 public static final int VISUAL_DEFAULT=0,VISUAL_LARGE_CHUNK=1,VISUAL_SWORD=2,VISUAL_AXE=3,VISUAL_TRIDENT=4,VISUAL_FLAMES=5,VISUAL_COUNT=6;
 public static final int RADIUS_MIN=1,RADIUS_MAX=10,RADIUS_DEFAULT=1,RADIUS_COUNT=RADIUS_MAX-RADIUS_MIN+1;
 public static final int SPHERE_FULL=0,SPHERE_UPPER_HEMISPHERE=1,SPHERE_LOWER_HEMISPHERE=2,SPHERE_MODE_COUNT=3,SPHERE_MODE_DEFAULT=SPHERE_FULL;
 public static final int DIMENSION_MIN=1,DIMENSION_MAX=10,DIMENSION_DEFAULT=1;
 public static final int BOX_SIZE_MIN=DIMENSION_MIN,BOX_SIZE_MAX=DIMENSION_MAX,BOX_SIZE_DEFAULT=DIMENSION_DEFAULT;
 public static final int DAMAGE_FIRE=0,DAMAGE_FROST=1,DAMAGE_FORCE=2,DAMAGE_PHYSICAL=3,DAMAGE_ARCANE=4,DAMAGE_ENTROPIC=5,DAMAGE_HOLY=6,DAMAGE_KIND_COUNT=7,DAMAGE_KIND_DEFAULT=DAMAGE_ARCANE;
 public static final int PROTECTION_ALL=0,PROTECTION_KIND_COUNT=DAMAGE_KIND_COUNT+1,PROTECTION_KIND_DEFAULT=PROTECTION_ALL;
 public static final int POTENCE_MIN=1,POTENCE_MAX=10,POTENCE_DEFAULT=1,POTENCE_COUNT=POTENCE_MAX-POTENCE_MIN+1;
 public static final int TARGET_BLOCKS=0,TARGET_ENTITIES=1,TARGET_ALL=2,TARGET_TYPE_COUNT=3,TARGET_TYPE_DEFAULT=TARGET_ALL;
 public static final int MOVE_PUSH=0,MOVE_PULL=1,MOVE_MODE_COUNT=2,MOVE_MODE_DEFAULT=MOVE_PUSH;
 public static final int IMPACT_OUTWARD=0,IMPACT_INWARD=1,IMPACT_DIRECTION_COUNT=2,IMPACT_DIRECTION_DEFAULT=IMPACT_OUTWARD;
 public static final int SIPHON_HEALTH=0,SIPHON_MANA=1,SIPHON_RESOURCE_COUNT=2,SIPHON_RESOURCE_DEFAULT=SIPHON_HEALTH;
 public static final int SIPHON_DRAIN=0,SIPHON_SEND=1,SIPHON_MODE_COUNT=2,SIPHON_MODE_DEFAULT=SIPHON_DRAIN;
 public static final int GRAVITY_ATTRACT=0,GRAVITY_REPEL=1,GRAVITY_MODE_COUNT=2,GRAVITY_MODE_DEFAULT=GRAVITY_ATTRACT;
 public static final int DURATION_MIN=1,DURATION_MAX=60,DURATION_DEFAULT=5;
 public static final int DISTANCE_MIN=1,DISTANCE_MAX=32,DISTANCE_DEFAULT=8;
 public static final int RELATIVE_FRONT=0,RELATIVE_BACK=1,RELATIVE_LEFT=2,RELATIVE_RIGHT=3,RELATIVE_UP=4,RELATIVE_DOWN=5,RELATIVE_DIRECTION_COUNT=6,RELATIVE_DIRECTION_DEFAULT=RELATIVE_FRONT;
 public static final int ALTER_SETTING_MIN=0,ALTER_SETTING_MAX=4,ALTER_SETTING_DEFAULT=2;
 public static final int TELEPORT_DIRECTIONAL=0,TELEPORT_BLINK=1,TELEPORT_HOME=2,TELEPORT_RECALL=3,TELEPORT_MODE_COUNT=4,TELEPORT_MODE_DEFAULT=TELEPORT_DIRECTIONAL;
 public static final int RANGE_VALUE_MIN=0,RANGE_VALUE_MAX=20,RANGE_VALUE_DEFAULT=10;
 public static final int SPLIT_POLYGONAL=0,SPLIT_LINEAR=1,SPLIT_PATTERN_COUNT=2,SPLIT_PATTERN_DEFAULT=SPLIT_POLYGONAL;
 private static final int LEGACY_SETTING_COUNT=7;
 private static final int PREVIOUS_SETTING_COUNT=8;
 private static final int PREVIOUS_SETTING_COUNT_2=9;
 private static final int PREVIOUS_SETTING_COUNT_3=10;
 private static final int PREVIOUS_SETTING_COUNT_4=12;
 private static final int PREVIOUS_SETTING_COUNT_5=14;
 private static final int PREVIOUS_SETTING_COUNT_6=15;
 private static final int PREVIOUS_SETTING_COUNT_7=16;
 private static final int PREVIOUS_SETTING_COUNT_8=19;
 private static final int PREVIOUS_SETTING_COUNT_9=20;
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
 public static int settingDefault(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_DEFAULT;case DAMAGE_KIND->DAMAGE_KIND_DEFAULT;case POTENCE->POTENCE_DEFAULT;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_DEFAULT;case TARGET_TYPE->TARGET_TYPE_DEFAULT;case DISTANCE->DISTANCE_DEFAULT;case MOVE_MODE->MOVE_MODE_DEFAULT;case IMPACT_DIRECTION->IMPACT_DIRECTION_DEFAULT;case SIPHON_RESOURCE->SIPHON_RESOURCE_DEFAULT;case SIPHON_MODE->SIPHON_MODE_DEFAULT;case DURATION_SECONDS->DURATION_DEFAULT;case GRAVITY_MODE->GRAVITY_MODE_DEFAULT;case SPHERE_MODE->SPHERE_MODE_DEFAULT;case PROTECTION_KIND->PROTECTION_KIND_DEFAULT;case ALTER_STRENGTH,ALTER_SPEED,ALTER_TOUGHNESS->ALTER_SETTING_DEFAULT;case TELEPORT_MODE->TELEPORT_MODE_DEFAULT;case RANGE_VALUE->RANGE_VALUE_DEFAULT;case SPLIT_PATTERN->SPLIT_PATTERN_DEFAULT;default->0;};}
 public static int settingMin(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_MIN;case DAMAGE_KIND->0;case POTENCE->POTENCE_MIN;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_MIN;case TARGET_TYPE->0;case DISTANCE->DISTANCE_MIN;case MOVE_MODE->0;case IMPACT_DIRECTION,SIPHON_RESOURCE,SIPHON_MODE,GRAVITY_MODE,SPHERE_MODE,PROTECTION_KIND->0;case DURATION_SECONDS->DURATION_MIN;case ALTER_STRENGTH,ALTER_SPEED,ALTER_TOUGHNESS->ALTER_SETTING_MIN;case TELEPORT_MODE,SPLIT_PATTERN->0;case RANGE_VALUE->RANGE_VALUE_MIN;default->0;};}
 public static int settingMax(SpellPropertyKey key){return switch(key){case RADIUS->RADIUS_MAX;case DAMAGE_KIND->DAMAGE_KIND_COUNT-1;case POTENCE->POTENCE_MAX;case WIDTH,HEIGHT,DEPTH->BOX_SIZE_MAX;case TARGET_TYPE->TARGET_TYPE_COUNT-1;case DISTANCE->DISTANCE_MAX;case MOVE_MODE->MOVE_MODE_COUNT-1;case IMPACT_DIRECTION->IMPACT_DIRECTION_COUNT-1;case SIPHON_RESOURCE->SIPHON_RESOURCE_COUNT-1;case SIPHON_MODE->SIPHON_MODE_COUNT-1;case DURATION_SECONDS->DURATION_MAX;case GRAVITY_MODE->RELATIVE_DIRECTION_COUNT-1;case SPHERE_MODE->SPHERE_MODE_COUNT-1;case PROTECTION_KIND->PROTECTION_KIND_COUNT-1;case ALTER_STRENGTH,ALTER_SPEED,ALTER_TOUGHNESS->ALTER_SETTING_MAX;case TELEPORT_MODE->TELEPORT_MODE_COUNT-1;case RANGE_VALUE->RANGE_VALUE_MAX;case SPLIT_PATTERN->SPLIT_PATTERN_COUNT-1;default->0;};}
 public static int clampSetting(SpellPropertyKey key,int value){return Mth.clamp(value,settingMin(key),settingMax(key));}
 public static int radiusAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.RADIUS);}
 public static int sphereHeightAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.HEIGHT);}
 public static int sphereModeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.SPHERE_MODE);}
 public static int damageKindAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DAMAGE_KIND);}
 public static int potenceAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.POTENCE);}
 public static int boxWidthAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.WIDTH);}
 public static int boxHeightAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.HEIGHT);}
 public static int boxDepthAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DEPTH);}
 public static int targetTypeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.TARGET_TYPE);}
 public static int distanceAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DISTANCE);}
 public static int moveModeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.MOVE_MODE);}
 public static int impactDirectionAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.IMPACT_DIRECTION);}
 public static int siphonResourceAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.SIPHON_RESOURCE);}
 public static int siphonModeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.SIPHON_MODE);}
 public static int durationSecondsAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.DURATION_SECONDS);}
 public static int gravityModeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.GRAVITY_MODE);}
 public static int protectionKindAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.PROTECTION_KIND);}
 public static int alterStrengthAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.ALTER_STRENGTH)-ALTER_SETTING_DEFAULT;}
 public static int alterSpeedAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.ALTER_SPEED)-ALTER_SETTING_DEFAULT;}
 public static int alterToughnessAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.ALTER_TOUGHNESS)-ALTER_SETTING_DEFAULT;}
 public static int teleportModeAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.TELEPORT_MODE);}
 public static int rangeValueAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.RANGE_VALUE)-RANGE_VALUE_DEFAULT;}
 public static int splitPatternAt(int[] settings,int cell){return settingAt(settings,cell,SpellPropertyKey.SPLIT_PATTERN);}
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
 public static String protectionKindName(int id){int value=Mth.clamp(id,0,PROTECTION_KIND_COUNT-1);return value==PROTECTION_ALL?"All":damageKindName(value-1);}
 public static int protectionDamageKind(int protectionKind){int value=Mth.clamp(protectionKind,0,PROTECTION_KIND_COUNT-1);return value==PROTECTION_ALL?-1:value-1;}
 public static String targetTypeName(int id){return switch(Mth.clamp(id,0,TARGET_TYPE_COUNT-1)){case TARGET_BLOCKS->"Blocks";case TARGET_ENTITIES->"Entities";default->"All";};}
 public static String moveModeName(int id){return Mth.clamp(id,0,MOVE_MODE_COUNT-1)==MOVE_PULL?"Pull":"Push";}
 public static String impactDirectionName(int id){return Mth.clamp(id,0,IMPACT_DIRECTION_COUNT-1)==IMPACT_INWARD?"Inward":"Outward";}
 public static String siphonResourceName(int id){return Mth.clamp(id,0,SIPHON_RESOURCE_COUNT-1)==SIPHON_MANA?"Mana":"Health";}
 public static String siphonModeName(int id){return Mth.clamp(id,0,SIPHON_MODE_COUNT-1)==SIPHON_SEND?"Send":"Drain";}
 public static String gravityModeName(int id){return Mth.clamp(id,0,GRAVITY_MODE_COUNT-1)==GRAVITY_REPEL?"Repel":"Attract";}
 public static String sphereModeName(int id){return switch(Mth.clamp(id,0,SPHERE_MODE_COUNT-1)){case SPHERE_UPPER_HEMISPHERE->"Upper Hemisphere";case SPHERE_LOWER_HEMISPHERE->"Lower Hemisphere";default->"Sphere";};}
 public static String relativeDirectionName(int id){return switch(Mth.clamp(id,0,RELATIVE_DIRECTION_COUNT-1)){case RELATIVE_BACK->"Back";case RELATIVE_LEFT->"Left";case RELATIVE_RIGHT->"Right";case RELATIVE_UP->"Up";case RELATIVE_DOWN->"Down";default->"Front";};}
 public static String teleportModeName(int id){return switch(Mth.clamp(id,0,TELEPORT_MODE_COUNT-1)){case TELEPORT_BLINK->"Blink";case TELEPORT_HOME->"Home";case TELEPORT_RECALL->"Recall";default->"Directional";};}
 public static String splitPatternName(int id){return Mth.clamp(id,0,SPLIT_PATTERN_COUNT-1)==SPLIT_LINEAR?"Linear":"Polygonal";}
 public static ItemStack visualStack(int id){return switch(Mth.clamp(id,0,VISUAL_COUNT-1)){case VISUAL_LARGE_CHUNK->stack(Items.STONE);case VISUAL_SWORD->stack(Items.IRON_SWORD);case VISUAL_AXE->stack(Items.IRON_AXE);case VISUAL_TRIDENT->stack(Items.TRIDENT);case VISUAL_FLAMES->stack(Items.FIRE_CHARGE);default->stack(Items.SNOWBALL);};}
 public static ItemStack stack(ItemLike item){return new ItemStack(item);}
 public static ItemStack sphereVisualStack(int style,int visual,int radius,int height,int mode,int lifetimeTicks){ItemStack stack=visualStack(visual);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","sphere_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_sphere_radius",Mth.clamp(radius,RADIUS_MIN,RADIUS_MAX));tag.putInt("sw_sphere_height",Mth.clamp(height,DIMENSION_MIN,DIMENSION_MAX));tag.putInt("sw_sphere_mode",Mth.clamp(mode,0,SPHERE_MODE_COUNT-1));tag.putInt("sw_visual_lifetime",Math.max(VisualDurationSupport.DEFAULT_VISUAL_TICKS,lifetimeTicks));});return stack;}
 public static ItemStack sphereVisualStack(int style,int visual,int radius,int height,int mode){return sphereVisualStack(style,visual,radius,height,mode,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}
 public static ItemStack sphereVisualStack(int style,int visual,int radius){return sphereVisualStack(style,visual,radius,radius,SPHERE_FULL);}
 public static ItemStack boxVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction,net.minecraft.world.phys.Vec3 up,int lifetimeTicks){ItemStack stack=visualStack(visual);net.minecraft.world.phys.Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);net.minecraft.world.phys.Vec3 upDir=up.lengthSqr()>1.0E-8?up.normalize():new net.minecraft.world.phys.Vec3(0.0,1.0,0.0);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","box_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_box_width",Mth.clamp(width,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_height",Mth.clamp(height,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putInt("sw_box_depth",Mth.clamp(depth,BOX_SIZE_MIN,BOX_SIZE_MAX));tag.putDouble("sw_box_dir_x",dir.x);tag.putDouble("sw_box_dir_y",dir.y);tag.putDouble("sw_box_dir_z",dir.z);tag.putDouble("sw_box_up_x",upDir.x);tag.putDouble("sw_box_up_y",upDir.y);tag.putDouble("sw_box_up_z",upDir.z);tag.putInt("sw_visual_lifetime",Math.max(VisualDurationSupport.DEFAULT_VISUAL_TICKS,lifetimeTicks));});return stack;}
 public static ItemStack boxVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction,net.minecraft.world.phys.Vec3 up){return boxVisualStack(style,visual,width,height,depth,direction,up,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}
 public static ItemStack coneVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction,int lifetimeTicks){ItemStack stack=visualStack(visual);net.minecraft.world.phys.Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","cone_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putInt("sw_cone_width",Mth.clamp(width,DIMENSION_MIN,DIMENSION_MAX));tag.putInt("sw_cone_height",Mth.clamp(height,DIMENSION_MIN,DIMENSION_MAX));tag.putInt("sw_cone_depth",Mth.clamp(depth,DIMENSION_MIN,DIMENSION_MAX));tag.putDouble("sw_cone_dir_x",dir.x);tag.putDouble("sw_cone_dir_y",dir.y);tag.putDouble("sw_cone_dir_z",dir.z);tag.putInt("sw_visual_lifetime",Math.max(VisualDurationSupport.DEFAULT_VISUAL_TICKS,lifetimeTicks));});return stack;}
 public static ItemStack coneVisualStack(int style,int visual,int width,int height,int depth,net.minecraft.world.phys.Vec3 direction){return coneVisualStack(style,visual,width,height,depth,direction,VisualDurationSupport.DEFAULT_VISUAL_TICKS);}

 public static ItemStack pointVisualStack(int style,int visual,net.minecraft.world.phys.Vec3 direction,int lifetimeTicks){ItemStack stack=visualStack(visual);net.minecraft.world.phys.Vec3 dir=direction!=null&&direction.lengthSqr()>1.0E-8?direction.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","point_visual");tag.putInt("sw_style",Mth.clamp(style,0,STYLE_COUNT-1));tag.putInt("sw_visual",Mth.clamp(visual,0,VISUAL_COUNT-1));tag.putDouble("sw_point_dir_x",dir.x);tag.putDouble("sw_point_dir_y",dir.y);tag.putDouble("sw_point_dir_z",dir.z);tag.putInt("sw_visual_lifetime",Math.max(VisualDurationSupport.DEFAULT_VISUAL_TICKS,lifetimeTicks));});return stack;}
 public static int readVisualLifetime(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("sw_visual_lifetime")?Math.max(VisualDurationSupport.DEFAULT_VISUAL_TICKS,tag.getInt("sw_visual_lifetime")):VisualDurationSupport.DEFAULT_VISUAL_TICKS;}
 public static net.minecraft.world.phys.Vec3 readPointVisualDirection(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();net.minecraft.world.phys.Vec3 dir=new net.minecraft.world.phys.Vec3(tag.getDouble("sw_point_dir_x"),tag.getDouble("sw_point_dir_y"),tag.getDouble("sw_point_dir_z"));return dir.lengthSqr()>1.0E-8?dir.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);}
 public static int readSphereVisualRadius(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_sphere_radius"),RADIUS_MIN,RADIUS_MAX);}
 public static int readSphereVisualHeight(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("sw_sphere_height")?Mth.clamp(tag.getInt("sw_sphere_height"),DIMENSION_MIN,DIMENSION_MAX):readSphereVisualRadius(stack);}
 public static int readSphereVisualMode(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("sw_sphere_mode")?Mth.clamp(tag.getInt("sw_sphere_mode"),0,SPHERE_MODE_COUNT-1):SPHERE_FULL;}
 public static int readBoxVisualWidth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_width"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualHeight(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_height"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static int readBoxVisualDepth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_box_depth"),BOX_SIZE_MIN,BOX_SIZE_MAX);}
 public static net.minecraft.world.phys.Vec3 readBoxVisualDirection(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();net.minecraft.world.phys.Vec3 dir=new net.minecraft.world.phys.Vec3(tag.getDouble("sw_box_dir_x"),tag.getDouble("sw_box_dir_y"),tag.getDouble("sw_box_dir_z"));return dir.lengthSqr()>1.0E-8?dir.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);}
 public static net.minecraft.world.phys.Vec3 readBoxVisualUp(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();net.minecraft.world.phys.Vec3 up=new net.minecraft.world.phys.Vec3(tag.getDouble("sw_box_up_x"),tag.getDouble("sw_box_up_y"),tag.getDouble("sw_box_up_z"));return up.lengthSqr()>1.0E-8?up.normalize():new net.minecraft.world.phys.Vec3(0.0,1.0,0.0);}
 public static int readConeVisualWidth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_width"),DIMENSION_MIN,DIMENSION_MAX);}
 public static int readConeVisualHeight(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_height"),DIMENSION_MIN,DIMENSION_MAX);}
 public static int readConeVisualDepth(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cone_depth"),DIMENSION_MIN,DIMENSION_MAX);}
 public static net.minecraft.world.phys.Vec3 readConeVisualDirection(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();net.minecraft.world.phys.Vec3 dir=new net.minecraft.world.phys.Vec3(tag.getDouble("sw_cone_dir_x"),tag.getDouble("sw_cone_dir_y"),tag.getDouble("sw_cone_dir_z"));return dir.lengthSqr()>1.0E-8?dir.normalize():new net.minecraft.world.phys.Vec3(0.0,0.0,1.0);}
 public static void writePlan(ItemStack stack,int[] source,int[] settingSource){int[] plan=normalizePlan(source),settings=normalizeSettings(settingSource);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","spell");tag.putIntArray("sw_plan",plan);tag.putIntArray("sw_settings",settings);});}
 public static void writeManaCosts(ItemStack stack,SpellManaCost.Costs costs){if(stack==null||costs==null)return;CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putFloat("sw_initial_cost",costs.initialCost());tag.putFloat("sw_initial_sustained_cost",costs.initialSustainedCost());tag.putFloat("sw_discount",costs.discount());tag.putFloat("sw_spell_cost",costs.spellCost());tag.putFloat("sw_sustained_cost",costs.sustainedCost());});}
 public static void ensureManaCosts(ItemStack stack){if(stack==null||stack.isEmpty())return;CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(tag.contains("sw_spell_cost")&&tag.contains("sw_sustained_cost")&&tag.contains("sw_initial_cost")&&tag.contains("sw_initial_sustained_cost")&&tag.contains("sw_discount"))return;writeManaCosts(stack,SpellManaCost.calculate(readPlan(stack),readSettings(stack)));}
 public static float readInitialCost(ItemStack stack){ensureManaCosts(stack);return Math.max(0.0F,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getFloat("sw_initial_cost"));}
 public static float readInitialSustainedCost(ItemStack stack){ensureManaCosts(stack);return Math.max(0.0F,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getFloat("sw_initial_sustained_cost"));}
 public static float readDiscount(ItemStack stack){ensureManaCosts(stack);return Math.max(0.0F,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getFloat("sw_discount"));}
 public static float readSpellCost(ItemStack stack){ensureManaCosts(stack);return Math.max(0.0F,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getFloat("sw_spell_cost"));}
 public static float readSustainedCost(ItemStack stack){ensureManaCosts(stack);return Math.max(0.0F,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getFloat("sw_sustained_cost"));}
 public static void applyDiscount(ItemStack stack,float discount){ensureManaCosts(stack);float initial=readInitialCost(stack),sustained=readInitialSustainedCost(stack),applied=Math.max(0.0F,discount);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putFloat("sw_discount",applied);tag.putFloat("sw_spell_cost",initial*applied);tag.putFloat("sw_sustained_cost",sustained*applied);});}
 public static int[] readPlan(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int[] raw=tag.getIntArray("sw_plan");if(raw.length>0)return normalizePlan(raw);int[] legacy=emptyPlan();if("missile".equals(tag.getString("sw_shape")))setCell(legacy,0,TYPE_MISSILE,tag.getInt("sw_style"),tag.getInt("sw_visual"));return legacy;}
 public static int[] readSettings(ItemStack stack){
  CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  int[] raw=tag.getIntArray("sw_settings");
  if(raw.length>0){
   int[] normalized=normalizeSettings(raw);
   if(raw.length!=SETTINGS_DATA_SIZE&&raw.length!=CELLS*PREVIOUS_SETTING_COUNT_7&&raw.length!=CELLS*PREVIOUS_SETTING_COUNT_8&&raw.length!=CELLS*PREVIOUS_SETTING_COUNT_9)migrateLegacySphereHeight(normalized,tag);
   return normalized;
  }
  int[] settings=emptySettings();
  importLegacy(settings,tag.getIntArray("sw_radius"),SpellPropertyKey.RADIUS);
  importLegacy(settings,tag.getIntArray("sw_damage_kind"),SpellPropertyKey.DAMAGE_KIND);
  importLegacy(settings,tag.getIntArray("sw_potence"),SpellPropertyKey.POTENCE);
  importLegacy(settings,tag.getIntArray("sw_box_width"),SpellPropertyKey.WIDTH);
  importLegacy(settings,tag.getIntArray("sw_box_height"),SpellPropertyKey.HEIGHT);
  importLegacy(settings,tag.getIntArray("sw_box_depth"),SpellPropertyKey.DEPTH);
  migrateLegacySphereHeight(settings,tag);
  return settings;
 }
 private static void migrateLegacySphereHeight(int[] settings,CompoundTag tag){int[] plan=normalizePlan(tag.getIntArray("sw_plan"));for(int cell=0;cell<CELLS;cell++)if(typeAt(plan,cell)==TYPE_SPHERE)setSetting(settings,cell,SpellPropertyKey.HEIGHT,radiusAt(settings,cell));}
 private static void importLegacy(int[] settings,int[] legacy,SpellPropertyKey key){if(legacy==null||legacy.length==0)return;for(int cell=0;cell<Math.min(CELLS,legacy.length);cell++)setSetting(settings,cell,key,legacy[cell]);}
 private static int[] normalizePlan(int[] source){int[] plan=emptyPlan();if(source!=null)System.arraycopy(source,0,plan,0,Math.min(source.length,plan.length));for(int i=0;i<CELLS;i++){int o=off(i);if(plan[o]!=TYPE_EMPTY&&SpellComponents.byType(plan[o])==null)plan[o]=TYPE_EMPTY;plan[o+1]=Mth.clamp(plan[o+1],0,STYLE_COUNT-1);plan[o+2]=Mth.clamp(plan[o+2],0,VISUAL_COUNT-1);}return plan;}
 private static int[] normalizeSettings(int[] source){
  int[] values=emptySettings();
  if(source!=null){
   if(source.length==CELLS*LEGACY_SETTING_COUNT){
    copySettingStride(source,LEGACY_SETTING_COUNT,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT){
    copySettingStride(source,PREVIOUS_SETTING_COUNT,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_2){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_2,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_3){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_3,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_4){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_4,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_5){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_5,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_6){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_6,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_7){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_7,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_8){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_8,values);
   }else if(source.length==CELLS*PREVIOUS_SETTING_COUNT_9){
    copySettingStride(source,PREVIOUS_SETTING_COUNT_9,values);
   }else if(source.length==SETTINGS_DATA_SIZE){
    System.arraycopy(source,0,values,0,values.length);
   }else{
    int guessedStride=source.length>0&&source.length%CELLS==0?source.length/CELLS:0;
    if(guessedStride>0)copySettingStride(source,guessedStride,values);
   }
  }
  for(int cell=0;cell<CELLS;cell++)for(SpellPropertyKey key:SpellPropertyKey.values())if(key.isSetting())values[settingOff(cell,key)]=clampSetting(key,values[settingOff(cell,key)]);
  return values;
 }
 private static void copySettingStride(int[] source,int sourceStride,int[] values){
  int copy=Math.min(sourceStride,SpellPropertyKey.SETTING_COUNT);
  for(int cell=0;cell<CELLS;cell++)for(int i=0;i<copy;i++){int from=cell*sourceStride+i;if(from<source.length)values[cell*SpellPropertyKey.SETTING_COUNT+i]=source[from];}
 }
 public static ItemStack projectileStack(int[] plan,int[] settings,int row,int cell,net.minecraft.world.phys.Vec3 castYaw,int activeDurationTicks,boolean blockInteraction){ItemStack stack=visualStack(visualAt(plan,cell));int[] planCopy=normalizePlan(plan),settingsCopy=normalizeSettings(settings);net.minecraft.world.phys.Vec3 yaw=SpellExecutor.normalizeYaw(castYaw);CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("sw_shape","missile");tag.putIntArray("sw_plan",planCopy);tag.putIntArray("sw_settings",settingsCopy);tag.putInt("sw_row",row);tag.putInt("sw_cell",cell);tag.putInt("sw_style",styleAt(planCopy,cell));tag.putInt("sw_visual",visualAt(planCopy,cell));tag.putDouble("sw_cast_yaw_x",yaw.x);tag.putDouble("sw_cast_yaw_z",yaw.z);tag.putInt("sw_scope_duration",Math.max(0,activeDurationTicks));tag.putBoolean("sw_scope_block_interaction",blockInteraction);});return stack;}
 public static int readRow(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_row");}
 public static int readCell(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_cell");}
 public static int readStyle(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_style"),0,STYLE_COUNT-1);}
 public static int readVisual(ItemStack stack){return Mth.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_visual"),0,VISUAL_COUNT-1);}
 public static int readScopeDuration(ItemStack stack){return Math.max(0,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("sw_scope_duration"));}
 public static boolean readScopeBlockInteraction(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("sw_scope_block_interaction");}
 public static net.minecraft.world.phys.Vec3 readCastYaw(ItemStack stack){CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return SpellExecutor.normalizeYaw(new net.minecraft.world.phys.Vec3(tag.getDouble("sw_cast_yaw_x"),0.0,tag.getDouble("sw_cast_yaw_z")));}
}
