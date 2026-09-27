package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public record ShapeResolution(Vec3 origin,Vec3 direction,List<BlockPos> voxels,Entity directEntity) {
 public static ShapeResolution point(Vec3 origin,Vec3 direction){
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  return new ShapeResolution(origin,dir,List.of(),null);
 }
 public static ShapeResolution impact(Vec3 origin,Vec3 direction,Entity directEntity){
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  return new ShapeResolution(origin,dir,List.of(),directEntity);
 }
 public static ShapeResolution sphere(Vec3 origin,Vec3 direction,int radius){
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  return new ShapeResolution(origin,dir,SphereShape.voxels(origin,radius),null);
 }
 public static ShapeResolution box(Vec3 origin,Vec3 direction,int width,int height,int depth){
  Vec3 dir=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  return new ShapeResolution(origin,dir,BoxShape.voxels(origin,width,height,depth),null);
 }
}
