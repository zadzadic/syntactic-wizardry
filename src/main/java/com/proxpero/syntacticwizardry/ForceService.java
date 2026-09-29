package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Shared physical movement backend for Move and future force-based mechanics. */
public final class ForceService {
    private ForceService() {}

    public static void apply(ServerLevel level, Entity owner, ShapeResolution resolution, int targetType, int mode, int potence) {
        boolean pull = mode == SpellPresentation.MOVE_PULL;
        if (targetType != SpellPresentation.TARGET_BLOCKS) {
            boolean directOwner = resolution.directEntity() == owner;
            for (Entity target : ResolvedTargets.entities(level, resolution)) {
                if ((target == owner && !directOwner) || target.isRemoved()) continue;
                Vec3 direction = vectorFor(resolution, target.position(), pull);
                MoveTravelRuntime.launch(target, direction, potence);
            }
        }
        if (targetType != SpellPresentation.TARGET_ENTITIES) {
            moveBlockArea(level, resolution, pull, potence);
        }
    }

    private static Vec3 vectorFor(ShapeResolution resolution, Vec3 target, boolean pull) {
        Vec3 base;
        VectorPolicy policy = resolution.vectorPolicy() == null ? VectorPolicy.FORWARD : resolution.vectorPolicy();
        if (resolution.directEntity() != null) {
            base = resolution.direction();
        } else if (policy == VectorPolicy.RADIAL) {
            base = target.subtract(resolution.origin());
        } else if (policy == VectorPolicy.SURFACE_NORMAL) {
            base = resolution.surfaceNormal() != null ? resolution.surfaceNormal() : resolution.direction();
        } else {
            base = resolution.direction();
        }
        if (base.lengthSqr() <= 1.0E-8) base = resolution.direction();
        if (base.lengthSqr() <= 1.0E-8) base = new Vec3(0.0, 1.0, 0.0);
        base = base.normalize();
        return pull ? base.scale(-1.0) : base;
    }

    /**
     * A block area begins as one rigid Move target.  Unobstructed voxels keep
     * the same offset and shared trajectory.  Terrain collision is resolved
     * per voxel by MovedBlockEntity, so an obstructed member can detach and
     * fall without stopping the rest of the moving area.
     */
    private static void moveBlockArea(ServerLevel level, ShapeResolution resolution, boolean pull, int potence) {
        LinkedHashSet<BlockPos> unique = new LinkedHashSet<>(resolution.voxels());
        if (unique.isEmpty()) return;

        List<BlockPos> eligible = new ArrayList<>(unique.size());
        double sx = 0.0D, sy = 0.0D, sz = 0.0D;
        for (BlockPos pos : unique) {
            BlockState state = level.getBlockState(pos);
            if (!MiningTierService.canAffect(level, pos, state, potence)) continue;
            eligible.add(pos.immutable());
            sx += pos.getX() + 0.5D;
            sy += pos.getY() + 0.5D;
            sz += pos.getZ() + 0.5D;
        }
        if (eligible.isEmpty()) return;

        Vec3 center = new Vec3(sx / eligible.size(), sy / eligible.size(), sz / eligible.size());
        Vec3 direction = vectorFor(resolution, center, pull);
        MovedBlockEntity.spawnRigidGroup(level, eligible, direction, potence);
    }
}
