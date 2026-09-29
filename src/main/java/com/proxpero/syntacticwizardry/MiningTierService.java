package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

/** Shared mining-tier eligibility used by Dig and block-moving mechanics. */
public final class MiningTierService {
    private MiningTierService() {}

    public static int tierForPotence(int potence) {
        return Math.max(1, Math.min(5, potence));
    }

    public static boolean canAffect(ServerLevel level, BlockPos pos, BlockState state, int potence) {
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty()) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;

        int tier = tierForPotence(potence);
        if (tier == 1) return state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return tier >= 5;
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) return tier >= 4;
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) return tier >= 3;
        return true;
    }
}
