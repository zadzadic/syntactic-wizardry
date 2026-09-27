package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
public final class TargetShape {
 private static final double RANGE=64.0;
 private TargetShape(){}
 public static ShapeResolution resolve(SpellExecutionContext context){
  ServerLevel level=context.level();
  Vec3 start=context.parent().origin();
  Vec3 direction=context.parent().direction().lengthSqr()>1.0E-8?context.parent().direction().normalize():new Vec3(0.0,0.0,1.0);
  Vec3 end=start.add(direction.scale(RANGE));
  BlockHitResult blockHit=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.owner()));
  boolean hasBlock=blockHit.getType()!=HitResult.Type.MISS;
  double blockDistance=hasBlock?start.distanceToSqr(blockHit.getLocation()):RANGE*RANGE;
  int targetType=context.targetType();
  EntitySelection entitySelection=null;
  if(targetType!=SpellPresentation.TARGET_BLOCKS)entitySelection=findEntity(context,start,end,blockDistance);
  if(targetType==SpellPresentation.TARGET_ENTITIES){
   return entitySelection==null?null:ShapeResolution.impact(entitySelection.location(),direction,entitySelection.entity());
  }
  if(targetType==SpellPresentation.TARGET_BLOCKS){
   return hasBlock?ShapeResolution.block(blockHit.getLocation(),direction,blockHit.getBlockPos()):null;
  }
  if(entitySelection!=null&&entitySelection.distanceSqr()<blockDistance)return ShapeResolution.impact(entitySelection.location(),direction,entitySelection.entity());
  return hasBlock?ShapeResolution.block(blockHit.getLocation(),direction,blockHit.getBlockPos()):null;
 }
 private static EntitySelection findEntity(SpellExecutionContext context,Vec3 start,Vec3 end,double maximumDistanceSqr){
  AABB search=new AABB(start,end).inflate(1.0);
  EntitySelection best=null;
  for(Entity entity:context.level().getEntities(context.owner(),search,e->isValidForNextRow(context,e))){
   AABB bounds=entity.getBoundingBox().inflate(entity.getPickRadius());
   Vec3 location;
   if(bounds.contains(start))location=start;
   else{
    Optional<Vec3> clipped=bounds.clip(start,end);
    if(clipped.isEmpty())continue;
    location=clipped.get();
   }
   double distance=start.distanceToSqr(location);
   if(distance>maximumDistanceSqr)continue;
   if(best==null||distance<best.distanceSqr())best=new EntitySelection(entity,location,distance);
  }
  return best;
 }
 private static boolean isValidForNextRow(SpellExecutionContext context,Entity entity){
  if(entity==context.owner()||entity.isRemoved()||!entity.isPickable()||entity.isSpectator())return false;
  int nextRow=context.row()+1;
  boolean hasEffect=false;
  if(nextRow<SpellPresentation.ROWS){
   for(int col=0;col<SpellPresentation.COLS;col++){
    SpellComponentDefinition definition=SpellComponents.byType(SpellPresentation.typeAt(context.plan(),nextRow*SpellPresentation.COLS+col));
    if(definition==null||definition.isShape())continue;
    hasEffect=true;
    if(definition.acceptsDirectEntity(entity))return true;
   }
  }
  return !hasEffect;
 }
 private record EntitySelection(Entity entity,Vec3 location,double distanceSqr){}
}
