package com.proxpero.syntacticwizardry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class ScribesLecternBlock extends Block {
 public static final MapCodec<ScribesLecternBlock> CODEC=simpleCodec(ScribesLecternBlock::new);
 public ScribesLecternBlock(Properties properties){super(properties);}
 @Override protected MapCodec<? extends Block> codec(){return CODEC;}
 @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
  if(!level.isClientSide && player instanceof ServerPlayer serverPlayer){
   MenuProvider provider=new SimpleMenuProvider((id,inventory,p)->new ScribesLecternMenu(id,inventory),Component.translatable("block.syntacticwizardry.scribes_lectern"));
   serverPlayer.openMenu(provider);
  }
  return InteractionResult.sidedSuccess(level.isClientSide);
 }
}
