package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class BoxShape {
 private BoxShape(){}
 public static List<BlockPos> voxels(Vec3 origin,int width,int height,int depth){
  int w=clamp(width),h=clamp(height),d=clamp(depth);
  BlockPos center=BlockPos.containing(origin);
  int minX=-(w-1)/2,maxX=w/2;
  int minY=-(h-1)/2,maxY=h/2;
  int minZ=-(d-1)/2,maxZ=d/2;
  ArrayList<BlockPos> out=new ArrayList<>(w*h*d);
  for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++)out.add(center.offset(x,y,z));
  return List.copyOf(out);
 }
 private static int clamp(int value){return Math.max(SpellPresentation.BOX_SIZE_MIN,Math.min(SpellPresentation.BOX_SIZE_MAX,value));}
}
