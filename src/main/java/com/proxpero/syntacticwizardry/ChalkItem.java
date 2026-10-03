package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ChalkItem extends Item {
    private final DyeColor color;

    public ChalkItem(Properties properties, DyeColor color) {
        super(properties);
        this.color=color;
    }

    public DyeColor color() { return color; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level=context.getLevel();
        Direction face=context.getClickedFace();
        BlockPos pos=context.getClickedPos().relative(face);

        if(!level.getBlockState(pos).canBeReplaced()) return InteractionResult.FAIL;
        ChalkRuneBlock block=ChalkRegistry.block();
        if(block==null) return InteractionResult.FAIL;

        BlockState state=block.defaultBlockState()
                .setValue(ChalkRuneBlock.FACING,face)
                .setValue(ChalkRuneBlock.GLYPH,level.getRandom().nextInt(16))
                .setValue(ChalkRuneBlock.COLOR,color);
        if(!state.canSurvive(level,pos)) return InteractionResult.FAIL;

        if(!level.isClientSide) {
            if(!level.setBlock(pos,state,3)) return InteractionResult.FAIL;
            damage(context.getItemInHand(),context.getPlayer());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void damage(ItemStack stack, Player player) {
        if(player!=null && player.getAbilities().instabuild) return;
        int next=stack.getDamageValue()+1;
        if(next>=stack.getMaxDamage()) stack.shrink(1);
        else stack.setDamageValue(next);
    }
}
