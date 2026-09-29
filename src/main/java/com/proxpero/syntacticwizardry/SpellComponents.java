package com.proxpero.syntacticwizardry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
public final class SpellComponents {
 public static final int TYPE_CHANNEL=10;
 public static final int TYPE_STREAM=11;
 public static final int TYPE_RESTORE=12;
 public static final int TYPE_MOVE=13;
 public static final int TYPE_SELF=14;
 public static final int TYPE_SIPHON=15;
 public static final int TYPE_GRAVITY=16;
 public static final int TYPE_DURATION=17;
 public static final int TYPE_BLOCK_INTERACTION=18;
 public static final int TYPE_RUNE=19;
 public static final int TYPE_RELATIVE=20;
 public static final int TYPE_CHAIN=21;
 public static final int TYPE_PROTECTION=22;
 public static final int TYPE_TEMPORARY_BLOCK=23;
 public static final int TYPE_ALTER=24;
 public static final int TYPE_FLIGHT=25;
 public static final int TYPE_DIMENSIONAL_STORAGE=26;
 public static final int TYPE_TELEPORTATION=27;
 public static final int TYPE_MARK=28;
 public static final int TYPE_RANGE=29;
 public static final int TYPE_SPLIT=30;
 public static final int TYPE_RICOCHET=31;
 public static final int TYPE_PIERCING=32;
 private static final SpellPropertyDefinition RADIUS_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.RADIUS,"Radius",SpellPropertyKind.STEPPER,SpellPresentation.RADIUS_MIN,SpellPresentation.RADIUS_MAX,SpellPresentation.RADIUS_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition SPHERE_MODE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.SPHERE_MODE,"Shape",SpellPropertyKind.OPTIONS,0,SpellPresentation.SPHERE_MODE_COUNT-1,SpellPresentation.SPHERE_MODE_DEFAULT,SpellPresentation::sphereModeName);
 private static final SpellPropertyDefinition POTENCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.POTENCE,"Potence",SpellPropertyKind.STEPPER,SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX,SpellPresentation.POTENCE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition DIG_POTENCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.POTENCE,"Potence",SpellPropertyKind.STEPPER,1,5,1,Integer::toString);
 private static final SpellPropertyDefinition FLIGHT_POTENCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.POTENCE,"Potence",SpellPropertyKind.STEPPER,1,8,1,Integer::toString);
 private static final SpellPropertyDefinition PROTECTION_KIND_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.PROTECTION_KIND,"Damage Type",SpellPropertyKind.OPTIONS,0,SpellPresentation.PROTECTION_KIND_COUNT-1,SpellPresentation.PROTECTION_KIND_DEFAULT,SpellPresentation::protectionKindName);
 private static final SpellPropertyDefinition DAMAGE_KIND_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DAMAGE_KIND,"Damage Type",SpellPropertyKind.OPTIONS,0,SpellPresentation.DAMAGE_KIND_COUNT-1,SpellPresentation.DAMAGE_KIND_DEFAULT,SpellPresentation::damageKindName);
 private static final SpellPropertyDefinition WIDTH_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.WIDTH,"Width",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition HEIGHT_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.HEIGHT,"Height",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition DEPTH_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DEPTH,"Depth",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition TARGET_TYPE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.TARGET_TYPE,"Target Type",SpellPropertyKind.OPTIONS,0,SpellPresentation.TARGET_TYPE_COUNT-1,SpellPresentation.TARGET_TYPE_DEFAULT,SpellPresentation::targetTypeName);
 private static final SpellPropertyDefinition TOUCH_TARGET_TYPE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.TARGET_TYPE,"Target Type",SpellPropertyKind.OPTIONS,SpellPresentation.TARGET_BLOCKS,SpellPresentation.TARGET_ENTITIES,SpellPresentation.TARGET_BLOCKS,SpellPresentation::targetTypeName);
 private static final SpellPropertyDefinition DISTANCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DISTANCE,"Distance",SpellPropertyKind.STEPPER,SpellPresentation.DISTANCE_MIN,SpellPresentation.DISTANCE_MAX,SpellPresentation.DISTANCE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition MOVE_MODE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.MOVE_MODE,"Mode",SpellPropertyKind.OPTIONS,0,SpellPresentation.MOVE_MODE_COUNT-1,SpellPresentation.MOVE_MODE_DEFAULT,SpellPresentation::moveModeName);
 private static final SpellPropertyDefinition IMPACT_DIRECTION_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.IMPACT_DIRECTION,"Impact Direction",SpellPropertyKind.OPTIONS,0,SpellPresentation.IMPACT_DIRECTION_COUNT-1,SpellPresentation.IMPACT_DIRECTION_DEFAULT,SpellPresentation::impactDirectionName);
 private static final SpellPropertyDefinition SIPHON_RESOURCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.SIPHON_RESOURCE,"Resource",SpellPropertyKind.OPTIONS,0,SpellPresentation.SIPHON_RESOURCE_COUNT-1,SpellPresentation.SIPHON_RESOURCE_DEFAULT,SpellPresentation::siphonResourceName);
 private static final SpellPropertyDefinition SIPHON_MODE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.SIPHON_MODE,"Mode",SpellPropertyKind.OPTIONS,0,SpellPresentation.SIPHON_MODE_COUNT-1,SpellPresentation.SIPHON_MODE_DEFAULT,SpellPresentation::siphonModeName);
 private static final SpellPropertyDefinition DURATION_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DURATION_SECONDS,"Duration",SpellPropertyKind.STEPPER,SpellPresentation.DURATION_MIN,SpellPresentation.DURATION_MAX,SpellPresentation.DURATION_DEFAULT,v->v+"s");
 private static final SpellPropertyDefinition GRAVITY_MODE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.GRAVITY_MODE,"Mode",SpellPropertyKind.OPTIONS,0,SpellPresentation.GRAVITY_MODE_COUNT-1,SpellPresentation.GRAVITY_MODE_DEFAULT,SpellPresentation::gravityModeName);
 private static final SpellPropertyDefinition RELATIVE_DIRECTION_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.GRAVITY_MODE,"Direction",SpellPropertyKind.OPTIONS,0,SpellPresentation.RELATIVE_DIRECTION_COUNT-1,SpellPresentation.RELATIVE_DIRECTION_DEFAULT,SpellPresentation::relativeDirectionName);
 private static final SpellPropertyDefinition TARGET_DIRECTION_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.IMPACT_DIRECTION,"Direction",SpellPropertyKind.OPTIONS,0,SpellPresentation.IMPACT_DIRECTION_COUNT-1,SpellPresentation.IMPACT_DIRECTION_DEFAULT,SpellPresentation::impactDirectionName);
 private static final SpellPropertyDefinition ALTER_STRENGTH_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.ALTER_STRENGTH,"Strength",SpellPropertyKind.STEPPER,SpellPresentation.ALTER_SETTING_MIN,SpellPresentation.ALTER_SETTING_MAX,SpellPresentation.ALTER_SETTING_DEFAULT,SpellComponents::alterSettingLabel);
 private static final SpellPropertyDefinition ALTER_SPEED_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.ALTER_SPEED,"Speed",SpellPropertyKind.STEPPER,SpellPresentation.ALTER_SETTING_MIN,SpellPresentation.ALTER_SETTING_MAX,SpellPresentation.ALTER_SETTING_DEFAULT,SpellComponents::alterSettingLabel);
 private static final SpellPropertyDefinition ALTER_TOUGHNESS_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.ALTER_TOUGHNESS,"Toughness",SpellPropertyKind.STEPPER,SpellPresentation.ALTER_SETTING_MIN,SpellPresentation.ALTER_SETTING_MAX,SpellPresentation.ALTER_SETTING_DEFAULT,SpellComponents::alterSettingLabel);
 private static final SpellPropertyDefinition TELEPORT_MODE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.TELEPORT_MODE,"Mode",SpellPropertyKind.OPTIONS,0,SpellPresentation.TELEPORT_MODE_COUNT-1,SpellPresentation.TELEPORT_MODE_DEFAULT,SpellPresentation::teleportModeName);
 private static final SpellPropertyDefinition RANGE_VALUE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.RANGE_VALUE,"Range",SpellPropertyKind.STEPPER,SpellPresentation.RANGE_VALUE_MIN,SpellPresentation.RANGE_VALUE_MAX,SpellPresentation.RANGE_VALUE_DEFAULT,SpellComponents::rangeValueLabel);
 private static final SpellPropertyDefinition SPLIT_PATTERN_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.SPLIT_PATTERN,"Pattern",SpellPropertyKind.OPTIONS,0,SpellPresentation.SPLIT_PATTERN_COUNT-1,SpellPresentation.SPLIT_PATTERN_DEFAULT,SpellPresentation::splitPatternName);
 private static final List<SpellComponentDefinition> ALL=List.of(new MissileDefinition(),new ChainDefinition(),new SphereDefinition(),new BoxDefinition(),new ConeDefinition(),new FloatingDefinition(),new TouchDefinition(),new TargetDefinition(),new DamageDefinition(),new DigDefinition(),new RestoreDefinition(),new MoveDefinition(),new SiphonDefinition(),new GravityDefinition(),new AlterDefinition(),new FlightDefinition(),new DimensionalStorageDefinition(),new TeleportationDefinition(),new MarkDefinition(),new ProtectionDefinition(),new TemporaryBlockDefinition(),new SelfDefinition(),new RelativeDefinition(),new RuneDefinition(),new ChannelDefinition(),new StreamDefinition(),new DurationDefinition(),new BlockInteractionDefinition(),new RangeDefinition(),new SplitDefinition(),new RicochetDefinition(),new PiercingDefinition());
 private static final Map<Integer,SpellComponentDefinition> BY_TYPE=ALL.stream().collect(Collectors.toUnmodifiableMap(SpellComponentDefinition::typeId,Function.identity()));
 private static final List<SpellComponentDefinition> SHAPES=ALL.stream().filter(SpellComponentDefinition::isShape).toList();
 private static final List<SpellComponentDefinition> EFFECTS=ALL.stream().filter(SpellComponents::isEffect).toList();
 private static final List<SpellComponentDefinition> MODIFIERS=ALL.stream().filter(SpellComponents::isModifier).toList();
 private SpellComponents(){}
 private static String alterSettingLabel(int stored){int value=stored-SpellPresentation.ALTER_SETTING_DEFAULT;return value>0?"+"+value:Integer.toString(value);}
 private static String rangeValueLabel(int stored){int value=stored-SpellPresentation.RANGE_VALUE_DEFAULT;return value>0?"+"+value:Integer.toString(value);}
 public static SpellComponentDefinition byType(int type){return BY_TYPE.get(type);}
 public static List<SpellComponentDefinition> all(){return ALL;}
 public static List<SpellComponentDefinition> shapes(){return SHAPES;}
 public static List<SpellComponentDefinition> effects(){return EFFECTS;}
 public static List<SpellComponentDefinition> modifiers(){return MODIFIERS;}
 public static boolean isModifier(SpellComponentDefinition definition){if(definition==null)return false;int type=definition.typeId();return type==TYPE_CHANNEL||type==TYPE_STREAM||type==TYPE_DURATION||type==TYPE_BLOCK_INTERACTION||type==TYPE_RANGE||type==TYPE_SPLIT||type==TYPE_RICOCHET||type==TYPE_PIERCING;}
 public static boolean isEffect(SpellComponentDefinition definition){return definition!=null&&!definition.isShape()&&!isModifier(definition);}
 public static boolean hasChannel(int[] plan){if(plan==null)return false;for(int cell=0;cell<SpellPresentation.CELLS;cell++)if(SpellPresentation.typeAt(plan,cell)==TYPE_CHANNEL)return true;return false;}
 public static boolean hasStream(int[] plan){if(plan==null)return false;for(int cell=0;cell<SpellPresentation.CELLS;cell++)if(SpellPresentation.typeAt(plan,cell)==TYPE_STREAM)return true;return false;}
 public static boolean hasAttachedModifier(int[] plan,int row,int ownerCol,int modifierType){return attachedModifierCell(plan,row,ownerCol,modifierType)>=0;}
 public static int attachedModifierCell(int[] plan,int row,int ownerCol,int modifierType){for(int col=ownerCol+1;col<SpellPresentation.COLS;col++){int cell=row*SpellPresentation.COLS+col;int type=SpellPresentation.typeAt(plan,cell);if(type==SpellPresentation.TYPE_EMPTY)continue;SpellComponentDefinition definition=byType(type);if(definition==null)continue;if(isModifier(definition)){if(type==modifierType)return cell;continue;}return -1;}return -1;}
 public static int attachedRangeValue(int[] plan,int[] settings,int row,int ownerCol){int cell=attachedModifierCell(plan,row,ownerCol,TYPE_RANGE);return cell<0?0:SpellPresentation.rangeValueAt(settings,cell);}
 public static int attachedSplitPotence(int[] plan,int[] settings,int row,int ownerCol){int cell=attachedModifierCell(plan,row,ownerCol,TYPE_SPLIT);return cell<0?0:SpellPresentation.potenceAt(settings,cell);}
 public static int attachedSplitPattern(int[] plan,int[] settings,int row,int ownerCol){int cell=attachedModifierCell(plan,row,ownerCol,TYPE_SPLIT);return cell<0?SpellPresentation.SPLIT_PATTERN_DEFAULT:SpellPresentation.splitPatternAt(settings,cell);}
 public static int attachedDurationTicks(int[] plan,int[] settings,int row,int ownerCol){for(int col=ownerCol+1;col<SpellPresentation.COLS;col++){int cell=row*SpellPresentation.COLS+col;int type=SpellPresentation.typeAt(plan,cell);if(type==SpellPresentation.TYPE_EMPTY)continue;SpellComponentDefinition definition=byType(type);if(definition==null)continue;if(isModifier(definition)){if(type==TYPE_DURATION)return SpellPresentation.durationSecondsAt(settings,cell)*20;continue;}return 0;}return 0;}
 public static boolean requiresHeldUse(int[] plan){return hasChannel(plan)||hasStream(plan);}
 public static boolean acceptsContinuationTarget(int[] plan,int resolvedRow,Entity entity){
  if(entity==null||entity.isRemoved()||!entity.isPickable()||entity.isSpectator())return false;
  int nextRow=resolvedRow+1;boolean hasEffect=false;
  if(nextRow<SpellPresentation.ROWS){for(int col=0;col<SpellPresentation.COLS;col++){SpellComponentDefinition definition=byType(SpellPresentation.typeAt(plan,nextRow*SpellPresentation.COLS+col));if(!isEffect(definition))continue;hasEffect=true;if(definition.acceptsDirectEntity(entity))return true;}}
  return !hasEffect;
 }
 public static int[] reducedChainSettings(int[] plan,int[] settings,int resolvedRow){
  int[] reduced=settings==null?SpellPresentation.emptySettings():java.util.Arrays.copyOf(settings,settings.length);
  for(int row=Math.max(0,resolvedRow+1);row<SpellPresentation.ROWS;row++)for(int col=0;col<SpellPresentation.COLS;col++){int cell=row*SpellPresentation.COLS+col;SpellComponentDefinition definition=byType(SpellPresentation.typeAt(plan,cell));if(!isEffect(definition)||!usesPotence(definition))continue;int current=SpellPresentation.potenceAt(reduced,cell);if(current<=1)return null;SpellPresentation.setSetting(reduced,cell,SpellPropertyKey.POTENCE,current-1);}
  return reduced;
 }
 private static boolean usesPotence(SpellComponentDefinition definition){for(SpellPropertyDefinition property:definition.settings())if(property.key()==SpellPropertyKey.POTENCE)return true;return false;}
 private abstract static class BaseDefinition implements SpellComponentDefinition {
  private final int typeId;private final String name;private final boolean shape;private final int defaultStyle;private final int defaultVisual;private final List<Integer> styleOptions;private final boolean supportsVisuals;private final List<SpellPropertyDefinition> settings;
  BaseDefinition(int typeId,String name,boolean shape,int defaultStyle,int defaultVisual,List<Integer> styleOptions,boolean supportsVisuals,List<SpellPropertyDefinition> settings){this.typeId=typeId;this.name=name;this.shape=shape;this.defaultStyle=defaultStyle;this.defaultVisual=defaultVisual;this.styleOptions=List.copyOf(styleOptions);this.supportsVisuals=supportsVisuals;this.settings=List.copyOf(settings);}
  @Override public int typeId(){return typeId;}@Override public String displayName(){return name;}@Override public boolean isShape(){return shape;}@Override public int defaultStyle(){return defaultStyle;}@Override public int defaultVisual(){return defaultVisual;}@Override public List<Integer> styleOptions(){return styleOptions;}@Override public boolean supportsVisuals(){return supportsVisuals;}@Override public List<SpellPropertyDefinition> settings(){return settings;}
 }
 private static final class MissileDefinition extends BaseDefinition {
  MissileDefinition(){super(SpellPresentation.TYPE_MISSILE,"Missile",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT,SpellPresentation.STYLE_ARC,SpellPresentation.STYLE_SPIRAL),true,List.of(IMPACT_DIRECTION_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){SpellMissile.spawnGroup(context);return ComponentExecutionResult.spawned();}
 }
 private static final class ChainDefinition extends BaseDefinition {
  ChainDefinition(){super(TYPE_CHAIN,"Chain",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT,SpellPresentation.STYLE_ARC,SpellPresentation.STYLE_SPIRAL),true,List.of(IMPACT_DIRECTION_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.CHAIN.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){SpellMissile.spawnGroup(context);return ComponentExecutionResult.spawned();}
 }
 private static final class SphereDefinition extends BaseDefinition {
  SphereDefinition(){super(SpellPresentation.TYPE_SPHERE,"Sphere",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT,SpellPresentation.STYLE_INNER,SpellPresentation.STYLE_OUTER),true,List.of(RADIUS_PROPERTY,HEIGHT_PROPERTY,SPHERE_MODE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.SPHERE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.sphere(context.areaOrigin(),context.shapeDirection(),context.shapeUp(),context.inheritedSurfaceNormal(),context.parent().vectorPolicy(),context.radius(),context.sphereHeight(),context.sphereMode());SphereVisualEntity visual=new SphereVisualEntity(context.level(),resolved.origin(),context.radius(),context.sphereHeight(),context.sphereMode(),context.style(),context.visual(),VisualDurationSupport.lifetimeTicksForShape(context.plan(),context.settings(),context.row()));boolean spawned=context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class BoxDefinition extends BaseDefinition {
  BoxDefinition(){super(SpellPresentation.TYPE_BOX,"Box",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT),true,List.of(WIDTH_PROPERTY,HEIGHT_PROPERTY,DEPTH_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.BOX_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.box(context.areaOrigin(),context.boxDirection(),context.shapeUp(),context.inheritedSurfaceNormal(),context.parent().vectorPolicy(),context.width(),context.height(),context.depth());BoxVisualEntity visual=new BoxVisualEntity(context.level(),resolved.origin(),resolved.direction(),resolved.up(),context.width(),context.height(),context.depth(),context.style(),context.visual(),VisualDurationSupport.lifetimeTicksForShape(context.plan(),context.settings(),context.row()));boolean spawned=context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class ConeDefinition extends BaseDefinition {
  ConeDefinition(){super(SpellPresentation.TYPE_CONE,"Cone",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT),true,List.of(DEPTH_PROPERTY,HEIGHT_PROPERTY,WIDTH_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.CONE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.cone(context.areaOrigin(),context.shapeDirection(),context.shapeUp(),context.inheritedSurfaceNormal(),context.parent().vectorPolicy(),context.width(),context.height(),context.depth());ConeVisualEntity visual=new ConeVisualEntity(context.level(),resolved.origin(),resolved.direction(),context.width(),context.height(),context.depth(),context.style(),context.visual(),VisualDurationSupport.lifetimeTicksForShape(context.plan(),context.settings(),context.row()));boolean spawned=context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class FloatingDefinition extends BaseDefinition {
  FloatingDefinition(){super(SpellPresentation.TYPE_FLOATING,"Floating",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of(DISTANCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.FLOATING_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.resolved(FloatingShape.resolve(context.parent().origin(),context.shapeDirection(),context.shapeUp(),context.inheritedSurfaceNormal(),context.distance()));}
 }
 private static final class TouchDefinition extends BaseDefinition {
  TouchDefinition(){super(SpellPresentation.TYPE_TOUCH,"Touch",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of(TOUCH_TARGET_TYPE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.TOUCH_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=TouchShape.resolve(context);return resolved==null?ComponentExecutionResult.spawned():ComponentExecutionResult.resolved(resolved);}
 }
 private static final class TargetDefinition extends BaseDefinition {
  TargetDefinition(){super(SpellPresentation.TYPE_TARGET,"Target",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of(TARGET_TYPE_PROPERTY,TARGET_DIRECTION_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.TARGET_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=TargetShape.resolve(context);return resolved==null?ComponentExecutionResult.spawned():ComponentExecutionResult.resolved(resolved);}
 }
 private static final class DigDefinition extends BaseDefinition {
  DigDefinition(){super(SpellPresentation.TYPE_DIG,"Dig",false,0,0,List.of(),false,List.of(DIG_POTENCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.DIG_EFFECT.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){AreaDigBulkRuntime.apply(context.level(),context.owner(),context.parent().voxels(),context.potence());return ComponentExecutionResult.NONE;}
 }
 private static final class DamageDefinition extends BaseDefinition {
  DamageDefinition(){super(SpellPresentation.TYPE_DAMAGE,"Damage",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,DAMAGE_KIND_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.DAMAGE_EFFECT.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){DamageEffect.apply(context.level(),context.owner(),context.parent(),context.damageKind(),context.potence());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
 }
 private static final class RestoreDefinition extends BaseDefinition {
  RestoreDefinition(){super(TYPE_RESTORE,"Restore",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.GLISTERING_MELON_SLICE.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){RestoreEffect.apply(context.level(),context.parent(),context.potence());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
 }
 private static final class MoveDefinition extends BaseDefinition {
  MoveDefinition(){super(TYPE_MOVE,"Move",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,TARGET_TYPE_PROPERTY,MOVE_MODE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.PISTON.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){MoveEffect.apply(context.level(),context.owner(),context.parent(),context.targetType(),context.moveMode(),context.potence());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity!=null&&!entity.isRemoved();}
 }
 private static final class SiphonDefinition extends BaseDefinition {
  SiphonDefinition(){super(TYPE_SIPHON,"Siphon",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,SIPHON_RESOURCE_PROPERTY,SIPHON_MODE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.GLISTERING_MELON_SLICE.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){SiphonEffect.apply(context.level(),context.owner(),context.parent(),context.siphonResource(),context.siphonMode(),context.potence());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
 }
 private static final class GravityDefinition extends BaseDefinition {
  GravityDefinition(){super(TYPE_GRAVITY,"Gravity",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,GRAVITY_MODE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.ENDER_PEARL.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){GravityEffect.apply(context.level(),context.owner(),context.parent(),context.potence(),context.gravityMode(),context.blockInteraction());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity!=null&&!entity.isRemoved();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.TICK;}
  @Override public boolean supportsBlockInteraction(){return true;}
 }
 private static final class AlterDefinition extends BaseDefinition {
  AlterDefinition(){super(TYPE_ALTER,"Alter",false,0,0,List.of(),false,List.of(ALTER_STRENGTH_PROPERTY,ALTER_SPEED_PROPERTY,ALTER_TOUGHNESS_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.POTION.getDefaultInstance();}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.CREATE_ONCE;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){AlterEffect.apply(context.level(),context.parent(),context.alterStrength(),context.alterSpeed(),context.alterToughness(),context.activeDurationTicks());return ComponentExecutionResult.NONE;}
 }
 private static final class FlightDefinition extends BaseDefinition {
  FlightDefinition(){super(TYPE_FLIGHT,"Flight",false,0,0,List.of(),false,List.of(FLIGHT_POTENCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.ELYTRA.getDefaultInstance();}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof net.minecraft.world.entity.player.Player player&&player.isAlive();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.STATEFUL;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){FlightEffect.apply(context.level(),context.parent(),context.potence(),context.activeDurationTicks());return ComponentExecutionResult.NONE;}
 }
 private static final class DimensionalStorageDefinition extends BaseDefinition {
  DimensionalStorageDefinition(){super(TYPE_DIMENSIONAL_STORAGE,"Dimensional Storage",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.ENDER_CHEST.getDefaultInstance();}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof net.minecraft.server.level.ServerPlayer player&&player.isAlive();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.CREATE_ONCE;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){DimensionalStorageEffect.apply(context);return ComponentExecutionResult.NONE;}
 }
 private static final class TeleportationDefinition extends BaseDefinition {
  TeleportationDefinition(){super(TYPE_TELEPORTATION,"Teleportation",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,TELEPORT_MODE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.ENDER_PEARL.getDefaultInstance();}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity!=null&&!entity.isRemoved();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.CREATE_ONCE;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){TeleportationEffect.apply(context);return ComponentExecutionResult.NONE;}
 }
 private static final class MarkDefinition extends BaseDefinition {
  MarkDefinition(){super(TYPE_MARK,"Mark",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.RECOVERY_COMPASS.getDefaultInstance();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.CREATE_ONCE;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){MarkEffect.apply(context);return ComponentExecutionResult.NONE;}
 }
 private static final class ProtectionDefinition extends BaseDefinition {
  ProtectionDefinition(){super(TYPE_PROTECTION,"Protection",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,PROTECTION_KIND_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.SHIELD.getDefaultInstance();}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.STATEFUL;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ProtectionEffect.apply(context.level(),context.parent(),context.protectionKind(),context.potence(),context.activeDurationTicks());return ComponentExecutionResult.NONE;}
 }
 private static final class TemporaryBlockDefinition extends BaseDefinition {
  TemporaryBlockDefinition(){super(TYPE_TEMPORARY_BLOCK,"Temporary Block",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.TEMPORARY_BLOCK_ITEM.get().getDefaultInstance();}
  @Override public EffectReplayPolicy replayPolicy(){return EffectReplayPolicy.CREATE_ONCE;}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){TemporaryBlockEffect.apply(context.level(),context.parent(),context.activeDurationTicks());return ComponentExecutionResult.NONE;}
 }
 private static final class SelfDefinition extends BaseDefinition {
  SelfDefinition(){super(TYPE_SELF,"Self",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.PLAYER_HEAD.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.resolved(SelfShape.resolve(context.owner(),context.shapeDirection()));}
 }

 private static final class RelativeDefinition extends BaseDefinition {
  RelativeDefinition(){super(TYPE_RELATIVE,"Relative",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of(RELATIVE_DIRECTION_PROPERTY,DISTANCE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.COMPASS.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.resolved(RelativeShape.resolve(context));}
 }

 private static final class RuneDefinition extends BaseDefinition {
  RuneDefinition(){super(TYPE_RUNE,"Rune",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.ENCHANTED_BOOK.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){RuneShape.place(context);return ComponentExecutionResult.spawned();}
 }
 private static final class ChannelDefinition extends BaseDefinition {
  ChannelDefinition(){super(TYPE_CHANNEL,"Channel",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.AMETHYST_SHARD.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class StreamDefinition extends BaseDefinition {
  StreamDefinition(){super(TYPE_STREAM,"Stream",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.PRISMARINE_SHARD.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class DurationDefinition extends BaseDefinition {
  DurationDefinition(){super(TYPE_DURATION,"Duration",false,0,0,List.of(),false,List.of(DURATION_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.CLOCK.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class BlockInteractionDefinition extends BaseDefinition {
  BlockInteractionDefinition(){super(TYPE_BLOCK_INTERACTION,"Block Interaction",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.IRON_PICKAXE.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class RangeDefinition extends BaseDefinition {
  RangeDefinition(){super(TYPE_RANGE,"Range",false,0,0,List.of(),false,List.of(RANGE_VALUE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.SPYGLASS.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class SplitDefinition extends BaseDefinition {
  SplitDefinition(){super(TYPE_SPLIT,"Split",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,SPLIT_PATTERN_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return Items.PRISMARINE_CRYSTALS.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class RicochetDefinition extends BaseDefinition {
  RicochetDefinition(){super(TYPE_RICOCHET,"Ricochet",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.SLIME_BALL.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
 private static final class PiercingDefinition extends BaseDefinition {
  PiercingDefinition(){super(TYPE_PIERCING,"Piercing",false,0,0,List.of(),false,List.of());}
  @Override public ItemStack createEditorIcon(){return Items.ARROW.getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){return ComponentExecutionResult.NONE;}
 }
}
