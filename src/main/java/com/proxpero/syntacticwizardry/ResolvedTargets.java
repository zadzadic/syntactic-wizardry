package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Generic target extraction from an already-resolved Shape. */
public final class ResolvedTargets {
    private ResolvedTargets() {}

    public static List<Entity> entities(ServerLevel level, ShapeResolution resolution) {
        return entities(level, resolution, null);
    }

    public static List<Entity> entities(ServerLevel level, ShapeResolution resolution, Entity excluded) {
        Entity direct = resolution.directEntity();
        if (direct != null && direct != excluded && !direct.isRemoved()) return List.of(direct);
        List<BlockPos> voxels = resolution.voxels();
        if (voxels.isEmpty()) return List.of();
        Occupancy occupancy = occupancy(voxels);
        List<Entity> result = new ArrayList<>();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, occupancy.bounds(), e -> e != excluded && !e.isRemoved())) {
            if (intersectsVoxel(entity.getBoundingBox(), occupancy.occupied())) result.add(entity);
        }
        return result;
    }

    public static List<LivingEntity> living(ServerLevel level, ShapeResolution resolution) {
        return living(level, resolution, null);
    }

    public static List<LivingEntity> living(ServerLevel level, ShapeResolution resolution, Entity excluded) {
        Entity direct = resolution.directEntity();
        if (direct != excluded && direct instanceof LivingEntity living && living.isAlive()) return List.of(living);
        List<BlockPos> voxels = resolution.voxels();
        if (voxels.isEmpty()) return List.of();
        Occupancy occupancy = occupancy(voxels);
        List<LivingEntity> result = new ArrayList<>();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, occupancy.bounds(), e -> e != excluded && e.isAlive())) {
            if (intersectsVoxel(living.getBoundingBox(), occupancy.occupied())) result.add(living);
        }
        return result;
    }

    private static Occupancy occupancy(List<BlockPos> voxels) {
        Set<Long> occupied = new HashSet<>(Math.max(16, voxels.size() * 2));
        int minX=Integer.MAX_VALUE,minY=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE;
        int maxX=Integer.MIN_VALUE,maxY=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
        for (BlockPos pos : voxels) {
            occupied.add(pos.asLong());
            minX=Math.min(minX,pos.getX()); minY=Math.min(minY,pos.getY()); minZ=Math.min(minZ,pos.getZ());
            maxX=Math.max(maxX,pos.getX()); maxY=Math.max(maxY,pos.getY()); maxZ=Math.max(maxZ,pos.getZ());
        }
        return new Occupancy(occupied,new AABB(minX,minY,minZ,maxX+1.0,maxY+1.0,maxZ+1.0));
    }

    private static boolean intersectsVoxel(AABB box, Set<Long> occupied) {
        int minX=(int)Math.floor(box.minX+1.0E-7),minY=(int)Math.floor(box.minY+1.0E-7),minZ=(int)Math.floor(box.minZ+1.0E-7);
        int maxX=(int)Math.floor(box.maxX-1.0E-7),maxY=(int)Math.floor(box.maxY-1.0E-7),maxZ=(int)Math.floor(box.maxZ-1.0E-7);
        for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++)if(occupied.contains(BlockPos.asLong(x,y,z)))return true;
        return false;
    }

    private record Occupancy(Set<Long> occupied,AABB bounds) {}
}
