package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Caster-centered Shape resolution. Direct Effects target the caster; later Shapes inherit the caster center. */
public final class SelfShape {
    private SelfShape() {}

    public static ShapeResolution resolve(Entity owner, Vec3 direction) {
        AABB bounds = owner.getBoundingBox();
        Vec3 center = new Vec3(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D
        );
        Vec3 forward = direction != null && direction.lengthSqr() > 1.0E-8
                ? direction.normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
        return new ShapeResolution(
                center,
                forward,
                new Vec3(0.0D, 1.0D, 0.0D),
                null,
                List.of(),
                owner,
                VectorPolicy.RADIAL
        );
    }
}
