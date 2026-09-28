package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public record ShapeResolution(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,List<BlockPos> voxels,Entity directEntity) {
 private static final Vec3 WORLD_UP=new Vec3(0.0,1.0,0.0);
 private static Vec3 normalize(Vec3 value,Vec3 fallback){return value!=null&&value.lengthSqr()>1.0E-8?value.normalize():fallback;}
 public static Vec3 surfaceUp(Vec3 normal){
  Vec3 n=normalize(normal,new Vec3(0.0,0.0,1.0));
  return Math.abs(n.y)>0.5?new Vec3(0.0,0.0,-1.0):WORLD_UP;
 }
 public static ShapeResolution point(Vec3 origin,Vec3 direction){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(),null);
 }
 public static ShapeResolution impact(Vec3 origin,Vec3 direction,Entity directEntity){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(),directEntity);
 }
 public static ShapeResolution block(Vec3 origin,Vec3 direction,BlockPos blockPos){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(blockPos.immutable()),null);
 }
 public static ShapeResolution surfaceBlock(BlockPos blockPos,Direction face){
  Vec3 normal=Vec3.atLowerCornerOf(face.getNormal());
  Vec3 center=Vec3.atCenterOf(blockPos);
  return new ShapeResolution(center,normal,surfaceUp(normal),normal,List.of(blockPos.immutable()),null);
 }
 public static ShapeResolution sphere(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int radius){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,SphereShape.voxels(origin,radius),null);
 }
 public static ShapeResolution box(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int width,int height,int depth){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,BoxShape.voxels(origin,dir,resolvedUp,width,height,depth),null);
 }
 public static ShapeResolution cone(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int width,int height,int depth){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,ConeShape.voxels(origin,dir,width,height,depth),null);
 }
 public static ShapeResolution floating(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int distance){
  int d=Math.max(SpellPresentation.DISTANCE_MIN,Math.min(SpellPresentation.DISTANCE_MAX,distance));
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 target=origin.add(dir.scale(d));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(target,dir,resolvedUp,normal,List.of(BlockPos.containing(target)),null);
 }
}
