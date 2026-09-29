package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PowerCoreSupport {
    private PowerCoreSupport() {}
    public static boolean isCorePart(BlockState state) {
        return state.is(SyntacticWizardry.POWER_CORE_CENTER.get())
                || state.is(SyntacticWizardry.POWER_CORE_CRYSTAL.get())
                || state.is(SyntacticWizardry.POWER_CORE_NW.get())
                || state.is(SyntacticWizardry.POWER_CORE_N.get())
                || state.is(SyntacticWizardry.POWER_CORE_NE.get())
                || state.is(SyntacticWizardry.POWER_CORE_W.get())
                || state.is(SyntacticWizardry.POWER_CORE_E.get())
                || state.is(SyntacticWizardry.POWER_CORE_SW.get())
                || state.is(SyntacticWizardry.POWER_CORE_S.get())
                || state.is(SyntacticWizardry.POWER_CORE_SE.get());
    }
    public static Block ringBlock(int dx, int dz) {
        if (dx==-1&&dz==-1) return SyntacticWizardry.POWER_CORE_NW.get();
        if (dx==0&&dz==-1) return SyntacticWizardry.POWER_CORE_N.get();
        if (dx==1&&dz==-1) return SyntacticWizardry.POWER_CORE_NE.get();
        if (dx==-1&&dz==0) return SyntacticWizardry.POWER_CORE_W.get();
        if (dx==1&&dz==0) return SyntacticWizardry.POWER_CORE_E.get();
        if (dx==-1&&dz==1) return SyntacticWizardry.POWER_CORE_SW.get();
        if (dx==0&&dz==1) return SyntacticWizardry.POWER_CORE_S.get();
        if (dx==1&&dz==1) return SyntacticWizardry.POWER_CORE_SE.get();
        throw new IllegalArgumentException("Not a ring offset");
    }
    public static boolean isRawStructure(Level level, BlockPos crystal) {
        if (!level.getBlockState(crystal).is(SyntacticWizardry.MATURE_CRYSTAL.get())) return false;
        BlockPos center=crystal.below();
        if (!level.getBlockState(center).is(Blocks.GOLD_BLOCK)) return false;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            if(dx==0&&dz==0) continue;
            if(!level.getBlockState(center.offset(dx,0,dz)).is(Blocks.NETHER_BRICKS)) return false;
        }
        return true;
    }
    public static void form(ServerLevel level, BlockPos crystal) {
        BlockPos center=crystal.below();
        level.setBlock(center,SyntacticWizardry.POWER_CORE_CENTER.get().defaultBlockState(),3);
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            if(dx==0&&dz==0) continue;
            level.setBlock(center.offset(dx,0,dz),ringBlock(dx,dz).defaultBlockState(),3);
        }
        level.setBlock(crystal,SyntacticWizardry.POWER_CORE_CRYSTAL.get().defaultBlockState(),3);
    }
    public static BlockPos centerFromPart(Level level, BlockPos pos, BlockState state) {
        if(state.is(SyntacticWizardry.POWER_CORE_CENTER.get())) return pos;
        if(state.is(SyntacticWizardry.POWER_CORE_CRYSTAL.get())) return pos.below();
        if(!isCorePart(state)) return null;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            BlockPos candidate=pos.offset(dx,0,dz);
            if(level.getBlockState(candidate).is(SyntacticWizardry.POWER_CORE_CENTER.get())) return candidate;
        }
        return null;
    }
    public static boolean isValid(ServerLevel level, BlockPos center) {
        if(!level.getBlockState(center).is(SyntacticWizardry.POWER_CORE_CENTER.get())) return false;
        if(!level.getBlockState(center.above()).is(SyntacticWizardry.POWER_CORE_CRYSTAL.get())) return false;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            if(dx==0&&dz==0) continue;
            if(!level.getBlockState(center.offset(dx,0,dz)).is(ringBlock(dx,dz))) return false;
        }
        return true;
    }
    public static int storageCapacity(ServerLevel level, BlockPos center) {
        if(!isValid(level,center)) return 0;
        int crystals=0;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            if(dx==0&&dz==0) continue;
            if(level.getBlockState(center.offset(dx,1,dz)).is(SyntacticWizardry.MATURE_CRYSTAL.get())) crystals++;
        }
        return (1+crystals)*50;
    }
    public static void clear(Level level, BlockPos center) {
        level.setBlock(center.above(),Blocks.AIR.defaultBlockState(),3);
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) level.setBlock(center.offset(dx,0,dz),Blocks.AIR.defaultBlockState(),3);
    }
}
