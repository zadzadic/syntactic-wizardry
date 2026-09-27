package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class SphereShape {
 private SphereShape(){}
 public static List<BlockPos> voxels(Vec3 origin,int radius){
  int r=Math.max(SpellPresentation.RADIUS_MIN,Math.min(SpellPresentation.RADIUS_MAX,radius));
  BlockPos center=BlockPos.containing(origin);
  int rr=r*r;
  ArrayList<BlockPos> out=new ArrayList<>();
  for(int y=-r;y<=r;y++)for(int z=-r;z<=r;z++)for(int x=-r;x<=r;x++){
   if(x*x+y*y+z*z<=rr)out.add(center.offset(x,y,z));
  }
  return List.copyOf(out);
 }
}
