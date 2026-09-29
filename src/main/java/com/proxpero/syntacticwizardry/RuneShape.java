package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** One-shot Rune Shape. Root casts touch a block face; chained casts inherit the parent spatial frame. */
public final class RuneShape {
    private static final double DIRECT_RANGE = 5.0D;

    private RuneShape() {}

    public static boolean place(SpellExecutionContext context) {
        Placement placement = context.rootCast() ? directPlacement(context) : inheritedPlacement(context);
        if (placement == null) return false;

        BlockPos runePos = placement.pos();
        if (!context.level().getBlockState(runePos).isAir()) return false;

        int glyph = context.level().getRandom().nextInt(6);
        Direction facing = closestDirection(placement.normal());
        BlockState runeState = SyntacticWizardry.RUNE_BLOCK.get().defaultBlockState()
                .setValue(RuneBlock.FACING, facing)
                .setValue(RuneBlock.GLYPH, glyph);

        if (!context.level().setBlock(runePos, runeState, 3)) return false;
        BlockEntity blockEntity = context.level().getBlockEntity(runePos);
        if (!(blockEntity instanceof RuneBlockEntity rune)) {
            context.level().removeBlock(runePos, false);
            return false;
        }
        rune.initialize(context, placement.normal(), placement.up());
        return true;
    }

    private static Placement directPlacement(SpellExecutionContext context) {
        Vec3 start = context.parent().origin();
        Vec3 direction = context.parent().direction().lengthSqr() > 1.0E-8D
                ? context.parent().direction().normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 end = start.add(direction.scale(DIRECT_RANGE));
        BlockHitResult hit = context.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, context.owner()));
        if (hit.getType() == HitResult.Type.MISS) return null;

        Direction face = hit.getDirection();
        BlockPos runePos = hit.getBlockPos().relative(face);
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal()).normalize();
        return new Placement(runePos.immutable(), normal, projectedUp(normal, context.castYaw()));
    }

    private static Placement inheritedPlacement(SpellExecutionContext context) {
        ShapeResolution parent = context.parent();
        Vec3 normal = parent.surfaceNormal() != null && parent.surfaceNormal().lengthSqr() > 1.0E-8D
                ? parent.surfaceNormal().normalize()
                : (parent.direction().lengthSqr() > 1.0E-8D ? parent.direction().normalize() : new Vec3(0.0D, 1.0D, 0.0D));
        Vec3 up = parent.up() != null && parent.up().lengthSqr() > 1.0E-8D
                ? parent.up().normalize()
                : projectedUp(normal, context.castYaw());

        BlockPos runePos = BlockPos.containing(parent.origin());
        if (!context.level().getBlockState(runePos).isAir()) {
            BlockPos shifted = runePos.relative(closestDirection(normal));
            if (!context.level().getBlockState(shifted).isAir()) return null;
            runePos = shifted;
        }
        return new Placement(runePos.immutable(), normal, up);
    }

    private static Vec3 projectedUp(Vec3 normal, Vec3 castYaw) {
        Vec3 yaw = SpellExecutor.normalizeYaw(castYaw);
        Vec3 projected = yaw.subtract(normal.scale(yaw.dot(normal)));
        return projected.lengthSqr() > 1.0E-8D ? projected.normalize() : ShapeResolution.surfaceUp(normal);
    }

    static Direction closestDirection(Vec3 vector) {
        double ax = Math.abs(vector.x), ay = Math.abs(vector.y), az = Math.abs(vector.z);
        if (ay >= ax && ay >= az) return vector.y >= 0.0D ? Direction.UP : Direction.DOWN;
        if (ax >= az) return vector.x >= 0.0D ? Direction.EAST : Direction.WEST;
        return vector.z >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private record Placement(BlockPos pos, Vec3 normal, Vec3 up) {}
}
