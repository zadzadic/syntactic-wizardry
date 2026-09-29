package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public final class TemporaryBlockRuntime {
 private static final Map<ServerLevel,Map<Long,Long>> EXPIRING=new IdentityHashMap<>();
 private TemporaryBlockRuntime(){}
 public static int create(ServerLevel level,List<BlockPos> voxels,int durationTicks){
  if(level==null||voxels==null||voxels.isEmpty())return 0;
  long expiry=durationTicks>0?level.getGameTime()+durationTicks:Long.MAX_VALUE;
  Map<Long,Long> levelEntries=EXPIRING.computeIfAbsent(level,ignored->new java.util.HashMap<>());
  int placed=0;
  for(BlockPos raw:voxels){
   BlockPos pos=raw.immutable();
   if(!level.getBlockState(pos).isAir()&&!level.getBlockState(pos).is(SyntacticWizardry.TEMPORARY_BLOCK.get()))continue;
   if(!level.getBlockState(pos).is(SyntacticWizardry.TEMPORARY_BLOCK.get())){
    if(!level.setBlock(pos,SyntacticWizardry.TEMPORARY_BLOCK.get().defaultBlockState(),3))continue;
    placed++;
   }
   long key=pos.asLong();
   long previous=levelEntries.getOrDefault(key,Long.MIN_VALUE);
   if(expiry>previous)levelEntries.put(key,expiry);
  }
  return placed;
 }
 public static void tick(MinecraftServer server){
  if(server==null||EXPIRING.isEmpty())return;
  Iterator<Map.Entry<ServerLevel,Map<Long,Long>>> levels=EXPIRING.entrySet().iterator();
  while(levels.hasNext()){
   Map.Entry<ServerLevel,Map<Long,Long>> levelEntry=levels.next();
   ServerLevel level=levelEntry.getKey();
   if(level.getServer()!=server)continue;
   long now=level.getGameTime();
   Iterator<Map.Entry<Long,Long>> entries=levelEntry.getValue().entrySet().iterator();
   while(entries.hasNext()){
    Map.Entry<Long,Long> entry=entries.next();
    BlockPos pos=BlockPos.of(entry.getKey());
    if(!level.getBlockState(pos).is(SyntacticWizardry.TEMPORARY_BLOCK.get())){entries.remove();continue;}
    if(entry.getValue()==Long.MAX_VALUE||now<entry.getValue())continue;
    level.removeBlock(pos,false);
    entries.remove();
   }
   if(levelEntry.getValue().isEmpty())levels.remove();
  }
 }
}
