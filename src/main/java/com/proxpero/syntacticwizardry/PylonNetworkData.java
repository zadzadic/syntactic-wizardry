package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class PylonNetworkData extends SavedData {
    private static final String DATA_NAME="syntacticwizardry_pylon_network_v1";
    public record Link(long pylon,long core){}
    private final Map<Long,LinkedHashSet<Long>> pylonsByChunk=new LinkedHashMap<>();
    private final Map<Long,Long> activeByChunk=new LinkedHashMap<>();
    private final LinkedHashSet<Link> links=new LinkedHashSet<>();
    public static Factory<PylonNetworkData> factory(){return new Factory<>(PylonNetworkData::new,PylonNetworkData::load,DataFixTypes.SAVED_DATA_COMMAND_STORAGE);}
    public static PylonNetworkData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(factory(),DATA_NAME);}
    public static PylonNetworkData load(CompoundTag tag,HolderLookup.Provider registries){
        PylonNetworkData data=new PylonNetworkData();
        ListTag pylons=tag.getList("Pylons",Tag.TAG_COMPOUND);
        for(int i=0;i<pylons.size();i++){CompoundTag e=pylons.getCompound(i);data.pylonsByChunk.computeIfAbsent(e.getLong("Chunk"),k->new LinkedHashSet<>()).add(e.getLong("Pos"));}
        ListTag active=tag.getList("Active",Tag.TAG_COMPOUND);
        for(int i=0;i<active.size();i++){CompoundTag e=active.getCompound(i);data.activeByChunk.put(e.getLong("Chunk"),e.getLong("Pos"));}
        ListTag links=tag.getList("Links",Tag.TAG_COMPOUND);
        for(int i=0;i<links.size();i++){CompoundTag e=links.getCompound(i);data.links.add(new Link(e.getLong("Pylon"),e.getLong("Core")));}
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        ListTag pylons=new ListTag();
        for(Map.Entry<Long,LinkedHashSet<Long>> entry:pylonsByChunk.entrySet()) for(long pos:entry.getValue()){CompoundTag e=new CompoundTag();e.putLong("Chunk",entry.getKey());e.putLong("Pos",pos);pylons.add(e);}
        tag.put("Pylons",pylons);
        ListTag active=new ListTag();
        for(Map.Entry<Long,Long> entry:activeByChunk.entrySet()){CompoundTag e=new CompoundTag();e.putLong("Chunk",entry.getKey());e.putLong("Pos",entry.getValue());active.add(e);}
        tag.put("Active",active);
        ListTag links=new ListTag();
        for(Link link:this.links){CompoundTag e=new CompoundTag();e.putLong("Pylon",link.pylon());e.putLong("Core",link.core());links.add(e);}
        tag.put("Links",links);return tag;
    }
    public void registerPylon(ServerLevel level,BlockPos middle){
        long chunk=chunkKey(middle.getX()>>4,middle.getZ()>>4);
        if(pylonsByChunk.computeIfAbsent(chunk,k->new LinkedHashSet<>()).add(middle.asLong()))setDirty();
        if(HighManaZones.isNaturallyHighMana(level,middle.getX()>>4,middle.getZ()>>4)&&!hasValidActive(level,chunk))chooseActive(level,chunk,Long.MIN_VALUE);
    }
    public void unregisterPylon(ServerLevel level,BlockPos middle){
        long chunk=chunkKey(middle.getX()>>4,middle.getZ()>>4),pos=middle.asLong();
        snapshotLinkedCores(level);boolean changed=false;
        LinkedHashSet<Long> pylons=pylonsByChunk.get(chunk);
        if(pylons!=null){changed|=pylons.remove(pos);if(pylons.isEmpty())pylonsByChunk.remove(chunk);}
        changed|=links.removeIf(link->link.pylon()==pos);
        Long active=activeByChunk.get(chunk);
        if(active!=null&&active==pos){activeByChunk.remove(chunk);chooseActive(level,chunk,pos);changed=true;}
        if(changed)setDirty();
    }
    public void unregisterCore(ServerLevel level,BlockPos core){
        snapshotLinkedCores(level);long pos=core.asLong();
        if(links.removeIf(link->link.core()==pos))setDirty();
        CoreManaData.get(level).remove(core);
    }
    public boolean link(ServerLevel level,BlockPos pylon,BlockPos core){
        if(!PylonSupport.isValid(level,pylon)||!PowerCoreSupport.isValid(level,core))return false;
        registerPylon(level,pylon);Link requested=new Link(pylon.asLong(),core.asLong());
        if(links.contains(requested))return false;
        snapshotLinkedCores(level);links.removeIf(link->link.pylon()==pylon.asLong());links.add(requested);setDirty();return true;
    }
    public boolean isSuppressed(ServerLevel level,int chunkX,int chunkZ){
        if(!HighManaZones.isNaturallyHighMana(level,chunkX,chunkZ))return false;
        long chunk=chunkKey(chunkX,chunkZ);
        if(!hasValidActive(level,chunk))chooseActive(level,chunk,Long.MIN_VALUE);
        return hasValidActive(level,chunk);
    }
    public boolean isActivePylon(ServerLevel level,BlockPos middle){
        int cx=middle.getX()>>4,cz=middle.getZ()>>4;
        if(!HighManaZones.isNaturallyHighMana(level,cx,cz))return false;
        long chunk=chunkKey(cx,cz);
        if(!hasValidActive(level,chunk))chooseActive(level,chunk,Long.MIN_VALUE);
        Long active=activeByChunk.get(chunk);
        return active!=null&&active==middle.asLong()&&PylonSupport.isValid(level,middle);
    }
    public int outputForCore(ServerLevel level,BlockPos core){
        if(!PowerCoreSupport.isValid(level,core))return 0;
        int output=0;long corePos=core.asLong();Iterator<Link> it=links.iterator();boolean changed=false;
        while(it.hasNext()){Link link=it.next();BlockPos pylon=BlockPos.of(link.pylon()),linkedCore=BlockPos.of(link.core());
            if(!PylonSupport.isValid(level,pylon)||!PowerCoreSupport.isValid(level,linkedCore)){it.remove();changed=true;continue;}
            if(link.core()==corePos&&isActivePylon(level,pylon))output++;
        }
        if(changed)setDirty();return output;
    }
    public List<BlockPos> activePylons(ServerLevel level){
        List<BlockPos> result=new ArrayList<>();Iterator<Map.Entry<Long,Long>> it=activeByChunk.entrySet().iterator();boolean changed=false;
        while(it.hasNext()){BlockPos pos=BlockPos.of(it.next().getValue());if(PylonSupport.isValid(level,pos))result.add(pos);else{it.remove();changed=true;}}
        if(changed)setDirty();return result;
    }
    public List<BlockPos> linkedCores(ServerLevel level){
        LinkedHashSet<Long> cores=new LinkedHashSet<>();Iterator<Link> it=links.iterator();boolean changed=false;
        while(it.hasNext()){Link link=it.next();if(!PylonSupport.isValid(level,BlockPos.of(link.pylon()))||!PowerCoreSupport.isValid(level,BlockPos.of(link.core()))){it.remove();changed=true;continue;}cores.add(link.core());}
        if(changed)setDirty();return cores.stream().map(BlockPos::of).toList();
    }
    private boolean hasValidActive(ServerLevel level,long chunk){
        Long active=activeByChunk.get(chunk);if(active==null)return false;
        if(PylonSupport.isValid(level,BlockPos.of(active)))return true;
        activeByChunk.remove(chunk);setDirty();return false;
    }
    private void chooseActive(ServerLevel level,long chunk,long excluded){
        LinkedHashSet<Long> set=pylonsByChunk.get(chunk);
        if(set==null||set.isEmpty()){activeByChunk.remove(chunk);return;}
        Iterator<Long> it=set.iterator();Long chosen=null;
        while(it.hasNext()){long pos=it.next();if(pos==excluded)continue;if(PylonSupport.isValid(level,BlockPos.of(pos))){chosen=pos;break;}it.remove();}
        if(set.isEmpty())pylonsByChunk.remove(chunk);
        if(chosen==null)activeByChunk.remove(chunk);else activeByChunk.put(chunk,chosen);
        setDirty();
    }
    private void snapshotLinkedCores(ServerLevel level){
        LinkedHashSet<Long> cores=new LinkedHashSet<>();for(Link link:links)cores.add(link.core());
        for(long core:cores){BlockPos pos=BlockPos.of(core);if(PowerCoreSupport.isValid(level,pos))ManaGridSupport.coreStored(level,pos);}
    }
    private static long chunkKey(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
}
