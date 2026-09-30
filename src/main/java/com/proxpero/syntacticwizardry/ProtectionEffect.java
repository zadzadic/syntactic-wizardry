package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public final class ProtectionEffect {
 private ProtectionEffect(){}
 public static void apply(ServerLevel level,Entity owner,ShapeResolution resolution,int protectionKind,int potence,int durationExtensionTicks,boolean excludeCaster){
  Entity direct=resolution.directEntity();
  if(direct instanceof LivingEntity living&&living.isAlive()&&(!excludeCaster||direct!=owner)){
   SpellProtectionService.apply(level,living,protectionKind,potence,durationExtensionTicks);
   return;
  }
  List<BlockPos> voxels=resolution.voxels();
  if(voxels.isEmpty())return;
  Set<Long> occupied=new HashSet<>(voxels.size()*2);
  int minX=Integer.MAX_VALUE,minY=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;
  int maxX=Integer.MIN_VALUE,maxY=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
  for(BlockPos pos:voxels){
   occupied.add(pos.asLong());
   minX=Math.min(minX,pos.getX());minY=Math.min(minY,pos.getY());minZ=Math.min(minZ,pos.getZ());
   maxX=Math.max(maxX,pos.getX());maxY=Math.max(maxY,pos.getY());maxZ=Math.max(maxZ,pos.getZ());
  }
  AABB bounds=new AABB(minX,minY,minZ,maxX+1.0,maxY+1.0,maxZ+1.0);
  for(LivingEntity living:level.getEntitiesOfClass(LivingEntity.class,bounds,e->e.isAlive()&&(!excludeCaster||e!=owner)))
   if(intersectsVoxel(living.getBoundingBox(),occupied))SpellProtectionService.apply(level,living,protectionKind,potence,durationExtensionTicks);
 }
 private static boolean intersectsVoxel(AABB box,Set<Long> occupied){
  int minX=Mth.floor(box.minX+1.0E-7),minY=Mth.floor(box.minY+1.0E-7),minZ=Mth.floor(box.minZ+1.0E-7);
  int maxX=Mth.floor(box.maxX-1.0E-7),maxY=Mth.floor(box.maxY-1.0E-7),maxZ=Mth.floor(box.maxZ-1.0E-7);
  for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++)if(occupied.contains(BlockPos.asLong(x,y,z)))return true;
  return false;
 }
}
