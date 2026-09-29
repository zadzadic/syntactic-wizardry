package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.List;
public record ShapeResolution(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,List<BlockPos> voxels,Entity directEntity,VectorPolicy vectorPolicy) {
 private static final Vec3 WORLD_UP=new Vec3(0.0,1.0,0.0);
 private static Vec3 normalize(Vec3 value,Vec3 fallback){return value!=null&&value.lengthSqr()>1.0E-8?value.normalize():fallback;}
 private static VectorPolicy policy(VectorPolicy value,Vec3 normal){return value!=null?value:(normal!=null?VectorPolicy.SURFACE_NORMAL:VectorPolicy.FORWARD);}
 public static Vec3 surfaceUp(Vec3 normal){
  Vec3 n=normalize(normal,new Vec3(0.0,0.0,1.0));
  return Math.abs(n.y)>0.5?new Vec3(0.0,0.0,-1.0):WORLD_UP;
 }
 public static ShapeResolution point(Vec3 origin,Vec3 direction){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(),null,VectorPolicy.FORWARD);
 }
 public static ShapeResolution impact(Vec3 origin,Vec3 direction,Entity directEntity){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(),directEntity,VectorPolicy.FORWARD);
 }
 public static ShapeResolution block(Vec3 origin,Vec3 direction,BlockPos blockPos){
  return new ShapeResolution(origin,normalize(direction,new Vec3(0.0,0.0,1.0)),WORLD_UP,null,List.of(blockPos.immutable()),null,VectorPolicy.FORWARD);
 }
 public static ShapeResolution projectileBlock(BlockPos blockPos,Direction face,int impactDirection){
  Vec3 outward=Vec3.atLowerCornerOf(face.getNormal()).normalize();
  boolean inward=impactDirection==SpellPresentation.IMPACT_INWARD;
  Vec3 direction=inward?outward.scale(-1.0):outward;
  BlockPos start=inward?blockPos:new BlockPos(blockPos.getX()+face.getNormal().getX(),blockPos.getY()+face.getNormal().getY(),blockPos.getZ()+face.getNormal().getZ());
  Vec3 origin=Vec3.atCenterOf(start);
  return new ShapeResolution(origin,direction,surfaceUp(direction),direction,List.of(start.immutable()),null,VectorPolicy.FORWARD);
 }
 public static ShapeResolution targetBlock(BlockPos blockPos,Direction face,int impactDirection){
  Vec3 outward=Vec3.atLowerCornerOf(face.getNormal()).normalize();
  boolean inward=impactDirection==SpellPresentation.IMPACT_INWARD;
  Vec3 direction=inward?outward.scale(-1.0):outward;
  BlockPos framePos=inward?blockPos:new BlockPos(blockPos.getX()+face.getNormal().getX(),blockPos.getY()+face.getNormal().getY(),blockPos.getZ()+face.getNormal().getZ());
  Vec3 origin=Vec3.atCenterOf(framePos);
  return new ShapeResolution(origin,direction,surfaceUp(direction),direction,List.of(blockPos.immutable()),null,VectorPolicy.FORWARD);
 }
 public static ShapeResolution surfaceBlock(BlockPos blockPos,Direction face){
  Vec3 normal=Vec3.atLowerCornerOf(face.getNormal());
  Vec3 center=Vec3.atCenterOf(blockPos);
  return new ShapeResolution(center,normal,surfaceUp(normal),normal,List.of(blockPos.immutable()),null,VectorPolicy.SURFACE_NORMAL);
 }
 public static ShapeResolution touchedBlock(Vec3 origin,Vec3 castDirection,BlockPos blockPos,Direction face){
  Vec3 direction=normalize(castDirection,new Vec3(0.0,0.0,1.0));
  Vec3 normal=Vec3.atLowerCornerOf(face.getNormal());
  return new ShapeResolution(origin,direction,surfaceUp(normal),normal,List.of(blockPos.immutable()),null,VectorPolicy.FORWARD);
 }
 public static ShapeResolution sphere(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int radius){return sphere(origin,direction,up,surfaceNormal,policy(null,surfaceNormal),radius,radius,SpellPresentation.SPHERE_FULL);}
 public static ShapeResolution sphere(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,VectorPolicy vectorPolicy,int radius){return sphere(origin,direction,up,surfaceNormal,vectorPolicy,radius,radius,SpellPresentation.SPHERE_FULL);}
 public static ShapeResolution sphere(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,VectorPolicy vectorPolicy,int radius,int height,int mode){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,SphereShape.voxels(origin,radius,height,mode),null,policy(vectorPolicy,normal));
 }
 public static ShapeResolution box(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int width,int height,int depth){return box(origin,direction,up,surfaceNormal,policy(null,surfaceNormal),width,height,depth);}
 public static ShapeResolution box(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,VectorPolicy vectorPolicy,int width,int height,int depth){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,BoxShape.voxels(origin,dir,resolvedUp,width,height,depth),null,policy(vectorPolicy,normal));
 }
 public static ShapeResolution cone(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int width,int height,int depth){return cone(origin,direction,up,surfaceNormal,policy(null,surfaceNormal),width,height,depth);}
 public static ShapeResolution cone(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,VectorPolicy vectorPolicy,int width,int height,int depth){
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(origin,dir,resolvedUp,normal,ConeShape.voxels(origin,dir,width,height,depth),null,policy(vectorPolicy,normal));
 }
 public static ShapeResolution floating(Vec3 origin,Vec3 direction,Vec3 up,Vec3 surfaceNormal,int distance){
  int d=Math.max(SpellPresentation.DISTANCE_MIN,Math.min(SpellPresentation.DISTANCE_MAX,distance));
  Vec3 dir=normalize(direction,new Vec3(0.0,0.0,1.0));
  Vec3 target=origin.add(dir.scale(d));
  Vec3 resolvedUp=normalize(up,WORLD_UP);
  Vec3 normal=surfaceNormal==null?null:normalize(surfaceNormal,dir);
  return new ShapeResolution(target,dir,resolvedUp,normal,List.of(BlockPos.containing((Position) target)),null,VectorPolicy.RADIAL);
 }
}
