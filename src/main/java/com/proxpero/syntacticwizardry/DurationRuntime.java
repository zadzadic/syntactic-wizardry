package com.proxpero.syntacticwizardry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
public final class DurationRuntime {
 private static final List<Invocation> ACTIVE=new ArrayList<>();
 private DurationRuntime(){}
 public static void register(SpellExecutionContext context,SpellComponentDefinition effect,int durationTicks){
  if(context==null||!supportsDuration(effect)||durationTicks<=1)return;
  long now=context.level().getGameTime();
  ACTIVE.add(new Invocation(context.level(),context.owner(),effect.typeId(),Arrays.copyOf(context.plan(),context.plan().length),Arrays.copyOf(context.settings(),context.settings().length),context.row(),context.cell(),context.parent(),context.castYaw(),context.blockInteraction(),now+1L,now+durationTicks));
 }
 public static void tick(MinecraftServer server){
  if(server==null||ACTIVE.isEmpty())return;
  Iterator<Invocation> iterator=ACTIVE.iterator();
  while(iterator.hasNext()){
   Invocation invocation=iterator.next();
   if(invocation.level.getServer()!=server)continue;
   if(invocation.owner==null||invocation.owner.isRemoved()){iterator.remove();continue;}
   long now=invocation.level.getGameTime();
   if(now>=invocation.endGameTime){iterator.remove();continue;}
   if(now<invocation.nextGameTime)continue;
   invocation.nextGameTime=now+1L;
   SpellComponentDefinition effect=SpellComponents.byType(invocation.effectType);
   if(!supportsDuration(effect)){iterator.remove();continue;}
   SpellExecutionContext replay=new SpellExecutionContext(invocation.level,invocation.owner,invocation.plan,invocation.settings,invocation.row,invocation.cell,invocation.resolution,false,invocation.castYaw,0,invocation.blockInteraction);
   effect.execute(replay);
  }
 }
 private static boolean supportsDuration(SpellComponentDefinition effect){
  if(!SpellComponents.isEffect(effect))return false;
  EffectReplayPolicy policy=effect.replayPolicy();
  return policy==EffectReplayPolicy.INSTANT||policy==EffectReplayPolicy.TICK;
 }
 private static final class Invocation {
  final ServerLevel level;final Entity owner;final int effectType;final int[] plan;final int[] settings;final int row;final int cell;final ShapeResolution resolution;final Vec3 castYaw;final boolean blockInteraction;long nextGameTime;final long endGameTime;
  Invocation(ServerLevel level,Entity owner,int effectType,int[] plan,int[] settings,int row,int cell,ShapeResolution resolution,Vec3 castYaw,boolean blockInteraction,long nextGameTime,long endGameTime){this.level=level;this.owner=owner;this.effectType=effectType;this.plan=plan;this.settings=settings;this.row=row;this.cell=cell;this.resolution=resolution;this.castYaw=castYaw;this.blockInteraction=blockInteraction;this.nextGameTime=nextGameTime;this.endGameTime=endGameTime;}
 }
}
