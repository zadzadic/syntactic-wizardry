package com.proxpero.syntacticwizardry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
public final class SpellComponents {
 private static final SpellPropertyDefinition RADIUS_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.RADIUS,"Radius",SpellPropertyKind.STEPPER,SpellPresentation.RADIUS_MIN,SpellPresentation.RADIUS_MAX,SpellPresentation.RADIUS_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition POTENCE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.POTENCE,"Potence",SpellPropertyKind.STEPPER,SpellPresentation.POTENCE_MIN,SpellPresentation.POTENCE_MAX,SpellPresentation.POTENCE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition DAMAGE_KIND_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DAMAGE_KIND,"Damage Type",SpellPropertyKind.OPTIONS,0,SpellPresentation.DAMAGE_KIND_COUNT-1,SpellPresentation.DAMAGE_KIND_DEFAULT,SpellPresentation::damageKindName);
 private static final SpellPropertyDefinition WIDTH_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.WIDTH,"Width",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition HEIGHT_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.HEIGHT,"Height",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition DEPTH_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.DEPTH,"Depth",SpellPropertyKind.STEPPER,SpellPresentation.BOX_SIZE_MIN,SpellPresentation.BOX_SIZE_MAX,SpellPresentation.BOX_SIZE_DEFAULT,Integer::toString);
 private static final SpellPropertyDefinition TARGET_TYPE_PROPERTY=new SpellPropertyDefinition(SpellPropertyKey.TARGET_TYPE,"Target Type",SpellPropertyKind.OPTIONS,0,SpellPresentation.TARGET_TYPE_COUNT-1,SpellPresentation.TARGET_TYPE_DEFAULT,SpellPresentation::targetTypeName);
 private static final List<SpellComponentDefinition> ALL=List.of(new MissileDefinition(),new SphereDefinition(),new BoxDefinition(),new ConeDefinition(),new TargetDefinition(),new DamageDefinition());
 private static final Map<Integer,SpellComponentDefinition> BY_TYPE=ALL.stream().collect(Collectors.toUnmodifiableMap(SpellComponentDefinition::typeId,Function.identity()));
 private static final List<SpellComponentDefinition> SHAPES=ALL.stream().filter(SpellComponentDefinition::isShape).toList();
 private static final List<SpellComponentDefinition> EFFECTS=ALL.stream().filter(def->!def.isShape()).toList();
 private SpellComponents(){}
 public static SpellComponentDefinition byType(int type){return BY_TYPE.get(type);}
 public static List<SpellComponentDefinition> all(){return ALL;}
 public static List<SpellComponentDefinition> shapes(){return SHAPES;}
 public static List<SpellComponentDefinition> effects(){return EFFECTS;}
 private abstract static class BaseDefinition implements SpellComponentDefinition {
  private final int typeId;private final String name;private final boolean shape;private final int defaultStyle;private final int defaultVisual;private final List<Integer> styleOptions;private final boolean supportsVisuals;private final List<SpellPropertyDefinition> settings;
  BaseDefinition(int typeId,String name,boolean shape,int defaultStyle,int defaultVisual,List<Integer> styleOptions,boolean supportsVisuals,List<SpellPropertyDefinition> settings){this.typeId=typeId;this.name=name;this.shape=shape;this.defaultStyle=defaultStyle;this.defaultVisual=defaultVisual;this.styleOptions=List.copyOf(styleOptions);this.supportsVisuals=supportsVisuals;this.settings=List.copyOf(settings);}
  @Override public int typeId(){return typeId;}@Override public String displayName(){return name;}@Override public boolean isShape(){return shape;}@Override public int defaultStyle(){return defaultStyle;}@Override public int defaultVisual(){return defaultVisual;}@Override public List<Integer> styleOptions(){return styleOptions;}@Override public boolean supportsVisuals(){return supportsVisuals;}@Override public List<SpellPropertyDefinition> settings(){return settings;}
 }
 private static final class MissileDefinition extends BaseDefinition {
  MissileDefinition(){super(SpellPresentation.TYPE_MISSILE,"Missile",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT,SpellPresentation.STYLE_ARC,SpellPresentation.STYLE_SPIRAL),true,List.of());}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.MISSILE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){SpellMissile missile=new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(),context.level());missile.prepare(context.owner(),context.parent().origin(),context.shapeDirection(),context.castYaw(),context.plan(),context.settings(),context.row(),context.cell());context.level().addFreshEntity(missile);return ComponentExecutionResult.spawned();}
 }
 private static final class SphereDefinition extends BaseDefinition {
  SphereDefinition(){super(SpellPresentation.TYPE_SPHERE,"Sphere",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT,SpellPresentation.STYLE_INNER,SpellPresentation.STYLE_OUTER),true,List.of(RADIUS_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.SPHERE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.sphere(context.areaOrigin(),context.shapeDirection(),context.radius());SphereVisualEntity visual=new SphereVisualEntity(context.level(),resolved.origin(),context.radius(),context.style(),context.visual());context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class BoxDefinition extends BaseDefinition {
  BoxDefinition(){super(SpellPresentation.TYPE_BOX,"Box",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT),true,List.of(WIDTH_PROPERTY,HEIGHT_PROPERTY,DEPTH_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.BOX_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.box(context.areaOrigin(),context.shapeDirection(),context.width(),context.height(),context.depth());BoxVisualEntity visual=new BoxVisualEntity(context.level(),resolved.origin(),context.width(),context.height(),context.depth(),context.style(),context.visual());context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class ConeDefinition extends BaseDefinition {
  ConeDefinition(){super(SpellPresentation.TYPE_CONE,"Cone",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(SpellPresentation.STYLE_DEFAULT),true,List.of(DEPTH_PROPERTY,HEIGHT_PROPERTY,WIDTH_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.CONE_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=ShapeResolution.cone(context.areaOrigin(),context.shapeDirection(),context.width(),context.height(),context.depth());ConeVisualEntity visual=new ConeVisualEntity(context.level(),resolved.origin(),resolved.direction(),context.width(),context.height(),context.depth(),context.style(),context.visual());context.level().addFreshEntity(visual);return ComponentExecutionResult.resolved(resolved);}
 }
 private static final class TargetDefinition extends BaseDefinition {
  TargetDefinition(){super(SpellPresentation.TYPE_TARGET,"Target",true,SpellPresentation.STYLE_DEFAULT,SpellPresentation.VISUAL_DEFAULT,List.of(),false,List.of(TARGET_TYPE_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.TARGET_SHAPE.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){ShapeResolution resolved=TargetShape.resolve(context);return resolved==null?ComponentExecutionResult.spawned():ComponentExecutionResult.resolved(resolved);}
 }
 private static final class DamageDefinition extends BaseDefinition {
  DamageDefinition(){super(SpellPresentation.TYPE_DAMAGE,"Damage",false,0,0,List.of(),false,List.of(POTENCE_PROPERTY,DAMAGE_KIND_PROPERTY));}
  @Override public ItemStack createEditorIcon(){return SyntacticWizardry.DAMAGE_EFFECT.get().getDefaultInstance();}
  @Override public ComponentExecutionResult execute(SpellExecutionContext context){DamageEffect.apply(context.level(),context.owner(),context.parent(),context.damageKind(),context.potence());return ComponentExecutionResult.NONE;}
  @Override public boolean acceptsDirectEntity(Entity entity){return entity instanceof LivingEntity living&&living.isAlive();}
 }
}
