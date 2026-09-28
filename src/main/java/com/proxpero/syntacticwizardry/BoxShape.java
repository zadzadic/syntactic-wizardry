package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class BoxShape {
 private static final double EPSILON=1.0E-7;
 private BoxShape(){}
 public static List<BlockPos> voxels(Vec3 origin,Vec3 direction,Vec3 upHint,int width,int height,int depth){
  int w=clamp(width),h=clamp(height),d=clamp(depth);
  Vec3 forward=direction.lengthSqr()>1.0E-8?direction.normalize():new Vec3(0.0,0.0,1.0);
  Vec3 up=orthogonalUp(forward,upHint);
  Vec3 right=up.cross(forward).normalize();
  up=forward.cross(right).normalize();
  Vec3 base=Vec3.atCenterOf(BlockPos.containing(origin));
  Vec3 center=base
   .add(right.scale((w&1)==0?0.5:0.0))
   .add(up.scale((h&1)==0?0.5:0.0))
   .add(forward.scale((d&1)==0?0.5:0.0));
  double halfW=w*0.5,halfH=h*0.5,halfD=d*0.5;
  double extentX=Math.abs(right.x)*halfW+Math.abs(up.x)*halfH+Math.abs(forward.x)*halfD;
  double extentY=Math.abs(right.y)*halfW+Math.abs(up.y)*halfH+Math.abs(forward.y)*halfD;
  double extentZ=Math.abs(right.z)*halfW+Math.abs(up.z)*halfH+Math.abs(forward.z)*halfD;
  int minX=(int)Math.floor(center.x-extentX-1.0),maxX=(int)Math.ceil(center.x+extentX+1.0);
  int minY=(int)Math.floor(center.y-extentY-1.0),maxY=(int)Math.ceil(center.y+extentY+1.0);
  int minZ=(int)Math.floor(center.z-extentZ-1.0),maxZ=(int)Math.ceil(center.z+extentZ+1.0);
  ArrayList<BlockPos> out=new ArrayList<>();
  for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++){
   Vec3 delta=new Vec3(x+0.5,y+0.5,z+0.5).subtract(center);
   double localX=delta.dot(right),localY=delta.dot(up),localZ=delta.dot(forward);
   if(Math.abs(localX)<halfW-EPSILON&&Math.abs(localY)<halfH-EPSILON&&Math.abs(localZ)<halfD-EPSILON)out.add(new BlockPos(x,y,z));
  }
  return List.copyOf(out);
 }
 private static Vec3 orthogonalUp(Vec3 forward,Vec3 hint){
  Vec3 candidate=hint!=null&&hint.lengthSqr()>1.0E-8?hint.normalize():new Vec3(0.0,1.0,0.0);
  Vec3 projected=candidate.subtract(forward.scale(candidate.dot(forward)));
  if(projected.lengthSqr()>1.0E-8)return projected.normalize();
  Vec3 fallback=Math.abs(forward.y)>0.9?new Vec3(0.0,0.0,-1.0):new Vec3(0.0,1.0,0.0);
  projected=fallback.subtract(forward.scale(fallback.dot(forward)));
  return projected.lengthSqr()>1.0E-8?projected.normalize():new Vec3(1.0,0.0,0.0);
 }
 private static int clamp(int value){return Math.max(SpellPresentation.BOX_SIZE_MIN,Math.min(SpellPresentation.BOX_SIZE_MAX,value));}
}
