package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;

public final class CoreManaData extends SavedData {
    private static final String DATA_NAME = "syntacticwizardry_core_mana_v1";
    private final Map<Long, Double> stored = new HashMap<>();
    private final Map<Long, Long> lastTick = new HashMap<>();
    public static Factory<CoreManaData> factory() { return new Factory<>(CoreManaData::new, CoreManaData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE); }
    public static CoreManaData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME); }
    public static CoreManaData load(CompoundTag tag, HolderLookup.Provider registries) {
        CoreManaData data = new CoreManaData();
        ListTag list = tag.getList("Cores", Tag.TAG_COMPOUND);
        for (int i=0;i<list.size();i++) {
            CompoundTag e=list.getCompound(i); long pos=e.getLong("Pos");
            data.stored.put(pos,e.getDouble("Stored")); data.lastTick.put(pos,e.getLong("LastTick"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list=new ListTag();
        for (Map.Entry<Long,Double> entry:stored.entrySet()) {
            CompoundTag e=new CompoundTag(); e.putLong("Pos",entry.getKey()); e.putDouble("Stored",entry.getValue()); e.putLong("LastTick",lastTick.getOrDefault(entry.getKey(),0L)); list.add(e);
        }
        tag.put("Cores",list); return tag;
    }
    public double update(ServerLevel level, BlockPos core, int capacity, int outputPerSecond) {
        long key=core.asLong(), now=level.getGameTime(), previous=lastTick.getOrDefault(key,now);
        double old=stored.getOrDefault(key,0.0D), current=Math.max(0.0D,Math.min(capacity,old));
        long elapsed=Math.max(0L,now-previous);
        if (elapsed>0L && outputPerSecond>0 && capacity>0) current=Math.min(capacity,current+elapsed*(outputPerSecond/20.0D));
        stored.put(key,current); lastTick.put(key,now);
        if (Math.abs(current-old)>1.0E-9D || previous!=now) setDirty();
        return current;
    }
    public double current(ServerLevel level, BlockPos core, int capacity, int outputPerSecond) { return update(level,core,capacity,outputPerSecond); }
    public double consume(ServerLevel level, BlockPos core, int capacity, int outputPerSecond, double amount) {
        double current=update(level,core,capacity,outputPerSecond), used=Math.min(Math.max(0.0D,amount),current);
        if (used>0.0D) { stored.put(core.asLong(),current-used); setDirty(); }
        return used;
    }
    public void remove(BlockPos core) {
        long key=core.asLong(); boolean changed=stored.remove(key)!=null; changed|=lastTick.remove(key)!=null; if(changed)setDirty();
    }
}
