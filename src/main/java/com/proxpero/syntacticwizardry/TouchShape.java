package com.proxpero.syntacticwizardry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
public final class TouchShape {
 private static final double RANGE=5.0;
 private TouchShape(){}
 public static ShapeResolution resolve(SpellExecutionContext context){
  ServerLevel level=context.level();
  Vec3 start=context.parent().origin();
  Vec3 direction=context.shapeDirection().lengthSqr()>1.0E-8?context.shapeDirection().normalize():new Vec3(0.0,0.0,1.0);
  Vec3 end=start.add(direction.scale(RANGE));
  BlockHitResult blockHit=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,context.owner()));
  boolean hasBlock=blockHit.getType()!=HitResult.Type.MISS;
  if(context.targetType()==SpellPresentation.TARGET_ENTITIES){
   double maxDistance=hasBlock?start.distanceToSqr(blockHit.getLocation()):RANGE*RANGE;
   EntitySelection entity=findEntity(context,start,end,maxDistance);
   return entity==null?null:ShapeResolution.impact(entity.location(),direction,entity.entity());
  }
  return hasBlock?ShapeResolution.surfaceBlock(blockHit.getBlockPos(),blockHit.getDirection()):null;
 }
 private static EntitySelection findEntity(SpellExecutionContext context,Vec3 start,Vec3 end,double maximumDistanceSqr){
  AABB search=new AABB(start,end).inflate(1.0);
  EntitySelection best=null;
  for(Entity entity:context.level().getEntities(context.owner(),search,e->e!=context.owner()&&!e.isRemoved()&&e.isPickable()&&!e.isSpectator())){
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
 private record EntitySelection(Entity entity,Vec3 location,double distanceSqr){}
}
