package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class BoxShape {
 private static final double EPSILON=1.0E-7;
 private BoxShape(){}
 public static List<BlockPos> voxels(Vec3 origin,Vec3 direction,int width,int height,int depth){
  int w=clamp(width),h=clamp(height),d=clamp(depth);
  Vec3 forward=SpellExecutor.normalizeYaw(direction);
  Vec3 right=new Vec3(forward.z,0.0,-forward.x);
  Vec3 up=new Vec3(0.0,1.0,0.0);
  Vec3 base=Vec3.atCenterOf(BlockPos.containing(origin));
  Vec3 center=base
   .add(right.scale((w&1)==0?0.5:0.0))
   .add(up.scale((h&1)==0?0.5:0.0))
   .add(forward.scale((d&1)==0?0.5:0.0));
  double halfW=w*0.5;
  double halfH=h*0.5;
  double halfD=d*0.5;
  double extentX=Math.abs(right.x)*halfW+Math.abs(forward.x)*halfD;
  double extentZ=Math.abs(right.z)*halfW+Math.abs(forward.z)*halfD;
  int minX=(int)Math.floor(center.x-extentX-1.0);
  int maxX=(int)Math.ceil(center.x+extentX+1.0);
  int minY=(int)Math.floor(center.y-halfH-1.0);
  int maxY=(int)Math.ceil(center.y+halfH+1.0);
  int minZ=(int)Math.floor(center.z-extentZ-1.0);
  int maxZ=(int)Math.ceil(center.z+extentZ+1.0);
  ArrayList<BlockPos> out=new ArrayList<>();
  for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++){
   Vec3 blockCenter=new Vec3(x+0.5,y+0.5,z+0.5);
   Vec3 delta=blockCenter.subtract(center);
   double localX=delta.dot(right);
   double localY=delta.y;
   double localZ=delta.dot(forward);
   if(Math.abs(localX)<halfW-EPSILON&&Math.abs(localY)<halfH-EPSILON&&Math.abs(localZ)<halfD-EPSILON)out.add(new BlockPos(x,y,z));
  }
  return List.copyOf(out);
 }
 private static int clamp(int value){return Math.max(SpellPresentation.BOX_SIZE_MIN,Math.min(SpellPresentation.BOX_SIZE_MAX,value));}
}
