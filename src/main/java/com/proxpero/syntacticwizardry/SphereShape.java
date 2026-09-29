package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
public final class SphereShape {
 private SphereShape(){}
 public static boolean containsOffset(int x,int y,int z,int radius,int height,int mode){
  int r=Math.max(SpellPresentation.RADIUS_MIN,Math.min(SpellPresentation.RADIUS_MAX,radius));
  int h=Math.max(SpellPresentation.DIMENSION_MIN,Math.min(SpellPresentation.DIMENSION_MAX,height));
  int m=Math.max(0,Math.min(SpellPresentation.SPHERE_MODE_COUNT-1,mode));
  if(m==SpellPresentation.SPHERE_UPPER_HEMISPHERE&&y<0)return false;
  if(m==SpellPresentation.SPHERE_LOWER_HEMISPHERE&&y>0)return false;
  long rr=(long)r*r,hh=(long)h*h;
  return ((long)x*x+(long)z*z)*hh+(long)y*y*rr<=rr*hh;
 }
 public static List<BlockPos> voxels(Vec3 origin,int radius,int height,int mode){
  int r=Math.max(SpellPresentation.RADIUS_MIN,Math.min(SpellPresentation.RADIUS_MAX,radius));
  int h=Math.max(SpellPresentation.DIMENSION_MIN,Math.min(SpellPresentation.DIMENSION_MAX,height));
  BlockPos center=BlockPos.containing((Position) origin);
  ArrayList<BlockPos> out=new ArrayList<>();
  for(int y=-h;y<=h;y++)for(int z=-r;z<=r;z++)for(int x=-r;x<=r;x++){
   if(containsOffset(x,y,z,r,h,mode))out.add(center.offset(x,y,z));
  }
  return List.copyOf(out);
 }
 public static List<BlockPos> voxels(Vec3 origin,int radius){return voxels(origin,radius,radius,SpellPresentation.SPHERE_FULL);}
}
