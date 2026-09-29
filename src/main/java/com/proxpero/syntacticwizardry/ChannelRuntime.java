package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public final class ChannelRuntime {
 private static final Map<UUID,List<Invocation>> ACTIVE=new HashMap<>();
 private ChannelRuntime(){}
 public static void begin(Entity owner){if(owner!=null)ACTIVE.remove(owner.getUUID());}
 public static void clear(Entity owner){if(owner!=null)ACTIVE.remove(owner.getUUID());}
 public static void register(SpellExecutionContext context,SpellComponentDefinition effect){
  if(context==null||effect==null||!SpellComponents.isEffect(effect))return;
  if(!(context.owner() instanceof LivingEntity living))return;
  if(!living.isUsingItem()||!living.getUseItem().is(SyntacticWizardry.WRITTEN_SPELL.get()))return;
  Invocation invocation=new Invocation(effect.typeId(),Arrays.copyOf(context.plan(),context.plan().length),Arrays.copyOf(context.settings(),context.settings().length),context.row(),context.cell(),context.parent(),context.castYaw(),context.blockInteraction());
  ACTIVE.computeIfAbsent(context.owner().getUUID(),ignored->new ArrayList<>()).add(invocation);
 }
 public static void tick(LivingEntity owner){
  if(owner==null||owner.level().isClientSide)return;
  List<Invocation> invocations=ACTIVE.get(owner.getUUID());
  if(invocations==null||invocations.isEmpty())return;
  if(!owner.isUsingItem()||!owner.getUseItem().is(SyntacticWizardry.WRITTEN_SPELL.get())){ACTIVE.remove(owner.getUUID());return;}
  if(!(owner.level() instanceof ServerLevel level)){ACTIVE.remove(owner.getUUID());return;}
  for(Invocation invocation:List.copyOf(invocations)){
   SpellComponentDefinition effect=SpellComponents.byType(invocation.effectType);
   if(!SpellComponents.isEffect(effect))continue;
   SpellExecutionContext replay=new SpellExecutionContext(level,owner,invocation.plan,invocation.settings,invocation.row,invocation.cell,invocation.resolution,false,invocation.castYaw,0,invocation.blockInteraction);
   effect.execute(replay);
  }
 }
 private static final class Invocation {
  final int effectType;final int[] plan;final int[] settings;final int row;final int cell;final ShapeResolution resolution;final net.minecraft.world.phys.Vec3 castYaw;final boolean blockInteraction;
  Invocation(int effectType,int[] plan,int[] settings,int row,int cell,ShapeResolution resolution,net.minecraft.world.phys.Vec3 castYaw,boolean blockInteraction){this.effectType=effectType;this.plan=plan;this.settings=settings;this.row=row;this.cell=cell;this.resolution=resolution;this.castYaw=castYaw;this.blockInteraction=blockInteraction;}
 }
}
