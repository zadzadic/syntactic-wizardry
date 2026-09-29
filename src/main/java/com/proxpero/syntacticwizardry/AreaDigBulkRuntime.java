package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public final class AreaDigBulkRuntime {
 private static final int UPDATE_FLAGS=2|16|32;
 private AreaDigBulkRuntime(){}
 public static int apply(ServerLevel level,Entity owner,List<BlockPos> voxels,int potence){
  if(voxels==null||voxels.isEmpty())return 0;
  int tier=Math.max(1,Math.min(5,potence));
  Set<Long> requested=new HashSet<>(Math.max(16,voxels.size()*2));
  ArrayList<BreakEntry> entries=new ArrayList<>(voxels.size());
  ArrayList<ItemStack> drops=new ArrayList<>();
  for(BlockPos raw:voxels){
   BlockPos pos=raw.immutable();
   if(!requested.add(pos.asLong()))continue;
   BlockState state=level.getBlockState(pos);
   if(!MiningTierService.canAffect(level,pos,state,tier))continue;
   BlockEntity blockEntity=level.getBlockEntity(pos);
   for(ItemStack drop:BlockBreakService.calculateDrops(level,owner,pos,state,blockEntity,tier))mergeDrop(drops,drop);
   entries.add(new BreakEntry(pos,state));
  }
  if(entries.isEmpty())return 0;
  Set<Long> broken=new HashSet<>(entries.size()*2);
  for(BreakEntry entry:entries){
   if(level.setBlock(entry.pos(),Blocks.AIR.defaultBlockState(),UPDATE_FLAGS))broken.add(entry.pos().asLong());
  }
  if(broken.isEmpty())return 0;
  for(BreakEntry entry:entries){
   if(!broken.contains(entry.pos().asLong()))continue;
   if(isBoundary(entry.pos(),broken))level.updateNeighborsAt(entry.pos(),entry.state().getBlock());
  }
  BlockPos dropPos=entries.get(0).pos();
  BlockBreakService.spawnDrops(level,dropPos,drops);
  level.playSound(null,dropPos,SoundEvents.STONE_BREAK,SoundSource.BLOCKS,1.0F,1.0F);
  return broken.size();
 }
 private static boolean isBoundary(BlockPos pos,Set<Long> broken){
  for(Direction direction:Direction.values())if(!broken.contains(pos.relative(direction).asLong()))return true;
  return false;
 }
 private static void mergeDrop(List<ItemStack> merged,ItemStack incoming){
  if(incoming==null||incoming.isEmpty())return;
  int remaining=incoming.getCount();
  for(ItemStack existing:merged){
   if(remaining<=0)break;
   if(!ItemStack.isSameItemSameComponents(existing,incoming))continue;
   int room=existing.getMaxStackSize()-existing.getCount();
   if(room<=0)continue;
   int moved=Math.min(room,remaining);
   existing.grow(moved);
   remaining-=moved;
  }
  while(remaining>0){
   ItemStack copy=incoming.copy();
   int moved=Math.min(copy.getMaxStackSize(),remaining);
   copy.setCount(moved);
   merged.add(copy);
   remaining-=moved;
  }
 }
 private record BreakEntry(BlockPos pos,BlockState state){}
}
