package com.proxpero.syntacticwizardry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class ScribesLecternBlock extends Block {
 public static final MapCodec<ScribesLecternBlock> CODEC=simpleCodec(ScribesLecternBlock::new);
 public ScribesLecternBlock(Properties properties){super(properties);}
 @Override protected MapCodec<? extends Block> codec(){return CODEC;}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(!level.isClientSide) player.openMenu((MenuProvider)state.getMenuProvider(level,pos));
  return InteractionResult.sidedSuccess(level.isClientSide);
 }
}
