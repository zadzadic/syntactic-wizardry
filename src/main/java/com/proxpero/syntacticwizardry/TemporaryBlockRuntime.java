package com.proxpero.syntacticwizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public final class TemporaryBlockRuntime {
 private record Entry(long expiryTick,BlockState replacedState){}
 private static final Map<ServerLevel,Map<Long,Entry>> ACTIVE=new IdentityHashMap<>();
 private TemporaryBlockRuntime(){}
 public static int create(ServerLevel level,List<BlockPos> voxels,int durationTicks){
  if(level==null||voxels==null||voxels.isEmpty())return 0;
  long expiry=durationTicks>0?level.getGameTime()+durationTicks:Long.MAX_VALUE;
  Map<Long,Entry> levelEntries=ACTIVE.computeIfAbsent(level,ignored->new java.util.HashMap<>());
  int placed=0;
  for(BlockPos raw:voxels){
   BlockPos pos=raw.immutable();
   BlockState current=level.getBlockState(pos);
   boolean alreadyTemporary=current.is(SyntacticWizardry.TEMPORARY_BLOCK.get());
   boolean water=current.is(Blocks.WATER);
   if(!alreadyTemporary&&!current.isAir()&&!water)continue;
   long key=pos.asLong();
   Entry previous=levelEntries.get(key);
   BlockState replaced=previous!=null?previous.replacedState():(water?current:null);
   if(!alreadyTemporary){
    if(!level.setBlock(pos,SyntacticWizardry.TEMPORARY_BLOCK.get().defaultBlockState(),3))continue;
    placed++;
   }
   long previousExpiry=previous==null?Long.MIN_VALUE:previous.expiryTick();
   levelEntries.put(key,new Entry(Math.max(expiry,previousExpiry),replaced));
  }
  return placed;
 }
 public static void dispel(ServerLevel level,List<BlockPos> voxels){
  if(level==null||voxels==null||voxels.isEmpty())return;
  Map<Long,Entry> levelEntries=ACTIVE.get(level);
  if(levelEntries==null)return;
  for(BlockPos raw:voxels){
   long key=raw.asLong();
   Entry entry=levelEntries.remove(key);
   if(entry==null)continue;
   restore(level,raw,entry);
  }
  if(levelEntries.isEmpty())ACTIVE.remove(level);
 }
 public static void tick(MinecraftServer server){
  if(server==null||ACTIVE.isEmpty())return;
  Iterator<Map.Entry<ServerLevel,Map<Long,Entry>>> levels=ACTIVE.entrySet().iterator();
  while(levels.hasNext()){
   Map.Entry<ServerLevel,Map<Long,Entry>> levelEntry=levels.next();
   ServerLevel level=levelEntry.getKey();
   if(level.getServer()!=server)continue;
   long now=level.getGameTime();
   Iterator<Map.Entry<Long,Entry>> entries=levelEntry.getValue().entrySet().iterator();
   while(entries.hasNext()){
    Map.Entry<Long,Entry> mapEntry=entries.next();
    BlockPos pos=BlockPos.of(mapEntry.getKey());
    Entry entry=mapEntry.getValue();
    BlockState current=level.getBlockState(pos);
    if(!current.is(SyntacticWizardry.TEMPORARY_BLOCK.get())){
     if(current.isAir()&&entry.replacedState()!=null)level.setBlock(pos,entry.replacedState(),3);
     entries.remove();
     continue;
    }
    if(entry.expiryTick()==Long.MAX_VALUE||now<entry.expiryTick())continue;
    restore(level,pos,entry);
    entries.remove();
   }
   if(levelEntry.getValue().isEmpty())levels.remove();
  }
 }
 private static void restore(ServerLevel level,BlockPos pos,Entry entry){
  if(!level.getBlockState(pos).is(SyntacticWizardry.TEMPORARY_BLOCK.get()))return;
  BlockState restored=entry.replacedState()!=null?entry.replacedState():Blocks.AIR.defaultBlockState();
  level.setBlock(pos,restored,3);
 }
}
