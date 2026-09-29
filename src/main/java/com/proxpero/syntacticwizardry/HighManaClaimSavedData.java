package com.proxpero.syntacticwizardry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistent per-player High Mana claims and compass targeting mode. */
public final class HighManaClaimSavedData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_high_mana_claims";

    public record Claim(ResourceLocation dimension, int chunkX, int chunkZ) {
        public int blockX() { return (chunkX << 4) + 8; }
        public int blockZ() { return (chunkZ << 4) + 8; }
    }

    private final Map<UUID, List<Claim>> claims = new HashMap<>();
    /** Missing entry means Search Mode. */
    private final Map<UUID, Claim> selectedTargets = new HashMap<>();

    public static Factory<HighManaClaimSavedData> factory() {
        return new Factory<>(HighManaClaimSavedData::new, HighManaClaimSavedData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public List<Claim> claims(UUID player) {
        List<Claim> stored = claims.get(player);
        return stored == null ? List.of() : List.copyOf(stored);
    }

    public boolean claim(UUID player, Claim claim) {
        if (player == null || claim == null) return false;
        List<Claim> stored = claims.computeIfAbsent(player, ignored -> new ArrayList<>());
        if (stored.contains(claim)) return false;
        stored.add(claim);
        setDirty();
        return true;
    }

    public boolean isSearchMode(UUID player) {
        return !selectedTargets.containsKey(player);
    }

    public Claim selectedTarget(UUID player) {
        Claim selected = selectedTargets.get(player);
        if (selected == null) return null;
        return claims(player).contains(selected) ? selected : null;
    }

    public void setSearchMode(UUID player) {
        if (selectedTargets.remove(player) != null) setDirty();
    }

    public boolean selectTarget(UUID player, Claim claim) {
        if (player == null || claim == null || !claims(player).contains(claim)) return false;
        selectedTargets.put(player, claim);
        setDirty();
        return true;
    }

    public static HighManaClaimSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        HighManaClaimSavedData data = new HighManaClaimSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            if (!playerTag.hasUUID("Player")) continue;
            UUID player = playerTag.getUUID("Player");
            ListTag list = playerTag.getList("Claims", Tag.TAG_COMPOUND);
            List<Claim> stored = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                CompoundTag claimTag = list.getCompound(j);
                ResourceLocation dimension = ResourceLocation.tryParse(claimTag.getString("Dimension"));
                if (dimension == null) continue;
                stored.add(new Claim(dimension, claimTag.getInt("ChunkX"), claimTag.getInt("ChunkZ")));
            }
            if (!stored.isEmpty()) data.claims.put(player, stored);

            if (playerTag.contains("Selected", Tag.TAG_COMPOUND)) {
                CompoundTag selectedTag = playerTag.getCompound("Selected");
                ResourceLocation dimension = ResourceLocation.tryParse(selectedTag.getString("Dimension"));
                if (dimension != null) {
                    Claim selected = new Claim(dimension, selectedTag.getInt("ChunkX"), selectedTag.getInt("ChunkZ"));
                    if (stored.contains(selected)) data.selectedTargets.put(player, selected);
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, List<Claim>> entry : claims.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            ListTag list = new ListTag();
            for (Claim claim : entry.getValue()) {
                CompoundTag claimTag = new CompoundTag();
                claimTag.putString("Dimension", claim.dimension().toString());
                claimTag.putInt("ChunkX", claim.chunkX());
                claimTag.putInt("ChunkZ", claim.chunkZ());
                list.add(claimTag);
            }
            playerTag.put("Claims", list);

            Claim selected = selectedTargets.get(entry.getKey());
            if (selected != null && entry.getValue().contains(selected)) {
                CompoundTag selectedTag = new CompoundTag();
                selectedTag.putString("Dimension", selected.dimension().toString());
                selectedTag.putInt("ChunkX", selected.chunkX());
                selectedTag.putInt("ChunkZ", selected.chunkZ());
                playerTag.put("Selected", selectedTag);
            }
            players.add(playerTag);
        }
        tag.put("Players", players);
        return tag;
    }
}
