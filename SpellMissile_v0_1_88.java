package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public final class SpellMissile extends Snowball {
 private static final double MAX_DISTANCE_SQR=16.0*16.0;
 private static final double CHAIN_SEARCH_RANGE=10.0;
 private static final double CHAIN_SEARCH_RANGE_SQR=CHAIN_SEARCH_RANGE*CHAIN_SEARCH_RANGE;
 private double startX,startY,startZ;
 private final Set<UUID> chainHitTargets=new HashSet<>();
 public SpellMissile(EntityType<? extends SpellMissile> type,Level level){super(type,level);}
 public SpellMissile(Level level,LivingEntity owner){this(SyntacticWizardry.SPELL_MISSILE.get(),level);setOwner(owner);setPos(owner.getX(),owner.getEyeY()-0.1,owner.getZ());markStart();}
 public void prepare(Entity owner,Vec3 origin,Vec3 direction,Vec3 castYaw,int[] plan,int[] settings,int row,int cell,int activeDurationTicks,boolean blockInteraction){
  setOwner(owner);
  setPos(origin.x,origin.y,origin.z);
  setItem(SpellPresentation.projectileStack(plan,settings,row,cell,castYaw,activeDurationTicks,blockInteraction));
  markStart();
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  shoot(dir.x,dir.y,dir.z,1.5F,0.0F);
 }
 public int presentationStyle(){return SpellPresentation.readStyle(getItem());}
 public int presentationVisual(){return SpellPresentation.readVisual(getItem());}
 private void markStart(){startX=getX();startY=getY();startZ=getZ();}
 private boolean isChainProjectile(){int[] plan=SpellPresentation.readPlan(getItem());return SpellPresentation.typeAt(plan,SpellPresentation.readCell(getItem()))==SpellComponents.TYPE_CHAIN;}
 @Override public boolean isNoGravity(){return true;}
 @Override protected boolean canHitEntity(Entity entity){
  if(!super.canHitEntity(entity))return false;
  if(!isChainProjectile())return true;
  if(entity==getOwner())return false;
  return !chainHitTargets.contains(entity.getUUID());
 }
 @Override public void tick(){super.tick();if(!level().isClientSide){double dx=getX()-startX,dy=getY()-startY,dz=getZ()-startZ;if(dx*dx+dy*dy+dz*dz>=MAX_DISTANCE_SQR)discard();}}
 @Override protected void onHit(HitResult result){
  if(!level().isClientSide&&level() instanceof ServerLevel server){
   int[] plan=SpellPresentation.readPlan(getItem());
   int[] settings=SpellPresentation.readSettings(getItem());
   Vec3 castYaw=SpellPresentation.readCastYaw(getItem());
   int row=SpellPresentation.readRow(getItem());
   int cell=SpellPresentation.readCell(getItem());
   boolean chain=SpellPresentation.typeAt(plan,cell)==SpellComponents.TYPE_CHAIN;
   Entity hit=result instanceof EntityHitResult entityHit?entityHit.getEntity():null;
   ShapeResolution resolved;
   if(result instanceof BlockHitResult blockHit){
    int impactDirection=SpellPresentation.impactDirectionAt(settings,cell);
    resolved=ShapeResolution.projectileBlock(blockHit.getBlockPos(),blockHit.getDirection(),impactDirection);
   }else{
    resolved=ShapeResolution.impact(result.getLocation(),getDeltaMovement(),hit);
   }
   SpellExecutor.continueFrom(server,getOwner(),plan,settings,row,resolved,castYaw,SpellPresentation.readScopeDuration(getItem()),SpellPresentation.readScopeBlockInteraction(getItem()));
   if(chain&&hit!=null&&hit!=getOwner()&&SpellComponents.acceptsContinuationTarget(plan,row,hit)){
    chainHitTargets.add(hit.getUUID());
    int[] reducedSettings=SpellComponents.reducedChainSettings(plan,settings,row);
    if(reducedSettings!=null){
     Entity nextTarget=findNearestChainTarget(server,result.getLocation(),plan,row);
     if(nextTarget!=null&&spawnChainJump(server,result.getLocation(),nextTarget,plan,reducedSettings,row,cell,castYaw)){
      discard();
      return;
     }
    }
   }
  }
  discard();
 }
 private Entity findNearestChainTarget(ServerLevel level,Vec3 origin,int[] plan,int row){
  AABB search=new AABB(origin,origin).inflate(CHAIN_SEARCH_RANGE);
  Entity best=null;double bestDistance=CHAIN_SEARCH_RANGE_SQR+1.0;
  for(Entity candidate:level.getEntities(this,search,entity->entity!=getOwner()&&!chainHitTargets.contains(entity.getUUID())&&SpellComponents.acceptsContinuationTarget(plan,row,entity))){
   Vec3 point=candidate.getBoundingBox().getCenter();
   double distance=origin.distanceToSqr(point);
   if(distance<=CHAIN_SEARCH_RANGE_SQR&&distance<bestDistance){best=candidate;bestDistance=distance;}
  }
  return best;
 }
 private boolean spawnChainJump(ServerLevel level,Vec3 origin,Entity target,int[] plan,int[] settings,int row,int cell,Vec3 castYaw){
  Vec3 targetPoint=target.getBoundingBox().getCenter();
  Vec3 direction=targetPoint.subtract(origin);
  if(direction.lengthSqr()<=1.0E-8)return false;
  SpellMissile next=new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(),level);
  next.prepare(getOwner(),origin,direction,castYaw,plan,settings,row,cell,SpellPresentation.readScopeDuration(getItem()),SpellPresentation.readScopeBlockInteraction(getItem()));
  next.chainHitTargets.addAll(chainHitTargets);
  return level.addFreshEntity(next);
 }
}
