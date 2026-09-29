package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public final class StreamRuntime {
 public static final int INTERVAL_TICKS=10;
 private static final Map<UUID,List<Invocation>> ACTIVE=new HashMap<>();
 private StreamRuntime(){}
 public static void begin(Entity owner){if(owner!=null)ACTIVE.remove(owner.getUUID());}
 public static void clear(Entity owner){if(owner!=null)ACTIVE.remove(owner.getUUID());}
 public static void register(SpellExecutionContext context,SpellComponentDefinition shape){
  if(context==null||shape==null||!shape.isShape())return;
  if(!(context.owner() instanceof LivingEntity living))return;
  if(!living.isUsingItem()||!living.getUseItem().is(SyntacticWizardry.WRITTEN_SPELL.get()))return;
  Invocation invocation=new Invocation(shape.typeId(),Arrays.copyOf(context.plan(),context.plan().length),Arrays.copyOf(context.settings(),context.settings().length),context.row(),context.cell(),context.parent(),context.rootCast(),context.activeDurationTicks(),context.blockInteraction(),INTERVAL_TICKS);
  ACTIVE.computeIfAbsent(context.owner().getUUID(),ignored->new ArrayList<>()).add(invocation);
 }
 public static void tick(LivingEntity owner){
  if(owner==null||owner.level().isClientSide)return;
  List<Invocation> invocations=ACTIVE.get(owner.getUUID());
  if(invocations==null||invocations.isEmpty())return;
  if(!owner.isUsingItem()||!owner.getUseItem().is(SyntacticWizardry.WRITTEN_SPELL.get())){ACTIVE.remove(owner.getUUID());return;}
  if(!(owner.level() instanceof ServerLevel level)){ACTIVE.remove(owner.getUUID());return;}
  for(Invocation invocation:List.copyOf(invocations)){
   if(--invocation.ticksUntilCast>0)continue;
   invocation.ticksUntilCast=INTERVAL_TICKS;
   SpellComponentDefinition shape=SpellComponents.byType(invocation.shapeType);
   if(shape==null||!shape.isShape())continue;
   Vec3 look=owner.getLookAngle();
   Vec3 yaw=yawFacing(owner.getYRot());
   ShapeResolution parent=invocation.rootCast?ShapeResolution.point(new Vec3(owner.getX(),owner.getEyeY()-0.1,owner.getZ()),look):invocation.parent;
   SpellExecutor.recastStreamShape(level,owner,invocation.plan,invocation.settings,invocation.row,invocation.cell,parent,invocation.rootCast,yaw,invocation.activeDurationTicks,invocation.blockInteraction);
  }
 }
 private static Vec3 yawFacing(float yawDegrees){
  double radians=Math.toRadians(yawDegrees);
  return new Vec3(-Math.sin(radians),0.0,Math.cos(radians));
 }
 private static final class Invocation {
  final int shapeType;
  final int[] plan;
  final int[] settings;
  final int row;
  final int cell;
  final ShapeResolution parent;
  final boolean rootCast;
  final int activeDurationTicks;
  final boolean blockInteraction;
  int ticksUntilCast;
  Invocation(int shapeType,int[] plan,int[] settings,int row,int cell,ShapeResolution parent,boolean rootCast,int activeDurationTicks,boolean blockInteraction,int ticksUntilCast){this.shapeType=shapeType;this.plan=plan;this.settings=settings;this.row=row;this.cell=cell;this.parent=parent;this.rootCast=rootCast;this.activeDurationTicks=activeDurationTicks;this.blockInteraction=blockInteraction;this.ticksUntilCast=ticksUntilCast;}
 }
}
