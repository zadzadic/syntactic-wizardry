package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LightEffect {
    public static final int ENTITY_DURATION_TICKS = 30 * 20;

    private LightEffect() {}

    public static void apply(SpellExecutionContext context) {
        if (context == null || context.level() == null || context.parent() == null) return;

        Entity direct = context.parent().directEntity();
        if (direct != null && !direct.isRemoved()) {
            LightEffectRuntime.lightEntity(context.level().getServer(), direct, ENTITY_DURATION_TICKS);
            return;
        }

        BlockPos target = resolvePlacement(context.level(), context.parent());
        if (target == null) return;

        int durationTicks = context.activeDurationTicks();
        LightEffectRuntime.placeStatic(context.level(), target, durationTicks);
    }

    private static BlockPos resolvePlacement(ServerLevel level, ShapeResolution resolution) {
        BlockPos origin = BlockPos.containing(resolution.origin());
        if (canReplace(level.getBlockState(origin))) return origin;

        if (resolution.surfaceNormal() != null && !resolution.voxels().isEmpty()) {
            BlockPos base = resolution.voxels().getFirst();
            int dx = (int)Math.round(resolution.surfaceNormal().x);
            int dy = (int)Math.round(resolution.surfaceNormal().y);
            int dz = (int)Math.round(resolution.surfaceNormal().z);
            BlockPos adjacent = base.offset(dx, dy, dz);
            if (canReplace(level.getBlockState(adjacent))) return adjacent;
        }

        for (BlockPos pos : resolution.voxels()) {
            if (canReplace(level.getBlockState(pos))) return pos.immutable();
        }

        return null;
    }

    static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.canBeReplaced();
    }
}
