package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class LuminalBridgeData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_luminal_bridges_v1";

    public static final class Entry {
        private final UUID id;
        private final ResourceLocation dimension;
        private final BlockPos center;
        private final Direction front;
        private final BlockPos portalMin;
        private final BlockPos portalMax;
        private String name;
        private boolean paused;
        private boolean powered;

        private Entry(
                UUID id,
                ResourceLocation dimension,
                BlockPos center,
                Direction front,
                BlockPos portalMin,
                BlockPos portalMax,
                String name,
                boolean paused,
                boolean powered) {
            this.id = id;
            this.dimension = dimension;
            this.center = center.immutable();
            this.front = front;
            this.portalMin = portalMin.immutable();
            this.portalMax = portalMax.immutable();
            this.name = sanitizeName(name);
            this.paused = paused;
            this.powered = powered;
        }

        public UUID id() { return id; }
        public ResourceLocation dimension() { return dimension; }
        public BlockPos center() { return center; }
        public BlockPos reliquaryPos() { return center.below(); }
        public Direction front() { return front; }
        public BlockPos portalMin() { return portalMin; }
        public BlockPos portalMax() { return portalMax; }
        public String name() { return name; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }

        public LuminalBridgeStructure.PortalGeometry geometry() {
            List<BlockPos> positions = rectanglePositions(front.getAxis(), portalMin, portalMax);
            return LuminalBridgeStructure.geometry(front, portalMin, portalMax, positions);
        }

        public boolean active() {
            return !paused && powered;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private LuminalBridgeData() {}

    public static Factory<LuminalBridgeData> factory() {
        return new Factory<>(LuminalBridgeData::new, LuminalBridgeData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static LuminalBridgeData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static LuminalBridgeData load(CompoundTag tag, HolderLookup.Provider registries) {
        LuminalBridgeData data = new LuminalBridgeData();
        ListTag list = tag.getList("Bridges", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            if (!entryTag.hasUUID("Id")) continue;
            ResourceLocation dimension = ResourceLocation.tryParse(entryTag.getString("Dimension"));
            if (dimension == null) continue;
            data.entries.add(new Entry(
                    entryTag.getUUID("Id"),
                    dimension,
                    BlockPos.of(entryTag.getLong("Center")),
                    Direction.from3DDataValue(entryTag.getInt("Front")),
                    BlockPos.of(entryTag.getLong("PortalMin")),
                    BlockPos.of(entryTag.getLong("PortalMax")),
                    entryTag.getString("Name"),
                    entryTag.getBoolean("Paused"),
                    entryTag.getBoolean("Powered")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Id", entry.id);
            entryTag.putString("Dimension", entry.dimension.toString());
            entryTag.putLong("Center", entry.center.asLong());
            entryTag.putInt("Front", entry.front.get3DDataValue());
            entryTag.putLong("PortalMin", entry.portalMin.asLong());
            entryTag.putLong("PortalMax", entry.portalMax.asLong());
            entryTag.putString("Name", entry.name);
            entryTag.putBoolean("Paused", entry.paused);
            entryTag.putBoolean("Powered", entry.powered);
            list.add(entryTag);
        }
        tag.put("Bridges", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry activate(ServerLevel level, BlockPos center, LuminalBridgeStructure.PortalGeometry geometry) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                level.dimension().location(),
                center,
                geometry.front(),
                geometry.min(),
                geometry.max(),
                "Luminal Bridge",
                false,
                RitualManaSupport.tryConsumeUpkeep(level, center, 1.0D));
        entries.add(entry);
        applyPortalState(level, entry);
        setDirty();
        return entry;
    }

    public boolean tick(MinecraftServer server) {
        boolean changed = false;
        boolean dirty = false;
        long gameTime = server.overworld().getGameTime();

        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            ServerLevel level = level(server, entry.dimension);
            if (level == null) continue;

            LuminalBridgeStructure.PortalGeometry geometry = entry.geometry();
            if (!LuminalBridgeStructure.activeStructureValid(level, entry.center, geometry)) {
                restoreRunes(level, entry);
                iterator.remove();
                changed = true;
                dirty = true;
                continue;
            }

            boolean previousPowered = entry.powered;
            if (entry.paused) {
                entry.powered = false;
            } else if (gameTime % 20L == 0L) {
                entry.powered = RitualManaSupport.tryConsumeUpkeep(level, entry.center, 1.0D);
            }

            if (entry.powered) ensurePortal(level, entry);
            else restoreRunes(level, entry);

            if (previousPowered != entry.powered) {
                changed = true;
                dirty = true;
            }
        }

        if (dirty) setDirty();
        return changed;
    }

    public Entry find(UUID id) {
        if (id == null) return null;
        for (Entry entry : entries) if (entry.id.equals(id)) return entry;
        return null;
    }

    public Entry findByPortal(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) return null;
        ResourceLocation dimension = level.dimension().location();
        for (Entry entry : entries) {
            if (!entry.dimension.equals(dimension)) continue;
            if (entry.geometry().contains(pos)) return entry;
        }
        return null;
    }

    public boolean setPaused(MinecraftServer server, UUID id, boolean paused) {
        Entry entry = find(id);
        if (entry == null || entry.paused == paused) return false;
        entry.paused = paused;
        ServerLevel level = level(server, entry.dimension);
        if (level != null) {
            if (paused) {
                entry.powered = false;
                restoreRunes(level, entry);
            } else {
                entry.powered = RitualManaSupport.tryConsumeUpkeep(level, entry.center, 1.0D);
                applyPortalState(level, entry);
            }
        }
        setDirty();
        return true;
    }

    public boolean rename(UUID id, String name) {
        Entry entry = find(id);
        if (entry == null) return false;
        String next = sanitizeName(name);
        if (entry.name.equals(next)) return false;
        entry.name = next;
        setDirty();
        return true;
    }

    public boolean stop(MinecraftServer server, UUID id) {
        Entry entry = find(id);
        if (entry == null) return false;
        ServerLevel level = level(server, entry.dimension);
        if (level != null) restoreRunes(level, entry);
        entries.remove(entry);
        setDirty();
        return true;
    }

    public boolean hasCodex(MinecraftServer server, Entry entry) {
        return codex(server, entry) != null;
    }

    public ItemStack codex(MinecraftServer server, Entry entry) {
        if (server == null || entry == null || LocationCodexRegistry.item() == null) return null;
        ServerLevel level = level(server, entry.dimension);
        if (level == null) return null;
        if (!(level.getBlockEntity(entry.reliquaryPos()) instanceof ReliquaryBlockEntity reliquary)) return null;
        ItemStack stack = reliquary.storedCodex();
        return !stack.isEmpty() && stack.is(LocationCodexRegistry.item()) ? stack : null;
    }

    public String destinationLabel(MinecraftServer server, Entry entry) {
        ItemStack codex = codex(server, entry);
        if (codex == null) return "";
        net.minecraft.network.chat.Component custom = codex.get(DataComponents.CUSTOM_NAME);
        if (custom != null && !custom.getString().isBlank()) return custom.getString();
        BlockPos pos = entry.center();
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static ServerLevel level(MinecraftServer server, ResourceLocation dimension) {
        if (server == null || dimension == null) return null;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
        return server.getLevel(key);
    }

    private static void applyPortalState(ServerLevel level, Entry entry) {
        if (entry.powered) ensurePortal(level, entry);
        else restoreRunes(level, entry);
    }

    private static void ensurePortal(ServerLevel level, Entry entry) {
        if (LuminalPortalRegistry.block() == null) return;
        for (BlockPos pos : entry.geometry().portalPositions()) {
            if (!level.getBlockState(pos).is(LuminalPortalRegistry.block())) {
                level.setBlock(pos, LuminalPortalRegistry.block().defaultBlockState(), 3);
            }
        }
    }

    private static void restoreRunes(ServerLevel level, Entry entry) {
        if (ChalkRegistry.block() == null) return;
        BlockState state = ChalkRegistry.block().defaultBlockState()
                .setValue(ChalkRuneBlock.FACING, entry.front)
                .setValue(ChalkRuneBlock.COLOR, DyeColor.WHITE)
                .setValue(ChalkRuneBlock.GLYPH, 0);
        for (BlockPos pos : entry.geometry().portalPositions()) {
            if (LuminalPortalRegistry.block() != null && level.getBlockState(pos).is(LuminalPortalRegistry.block())) {
                level.setBlock(pos, state, 3);
            }
        }
    }

    private static List<BlockPos> rectanglePositions(Direction.Axis axis, BlockPos min, BlockPos max) {
        List<BlockPos> result = new ArrayList<>();
        switch (axis) {
            case X -> {
                int x = min.getX();
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) result.add(new BlockPos(x, y, z));
                }
            }
            case Y -> {
                int y = min.getY();
                for (int x = min.getX(); x <= max.getX(); x++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) result.add(new BlockPos(x, y, z));
                }
            }
            case Z -> {
                int z = min.getZ();
                for (int x = min.getX(); x <= max.getX(); x++) {
                    for (int y = min.getY(); y <= max.getY(); y++) result.add(new BlockPos(x, y, z));
                }
            }
        }
        return List.copyOf(result);
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Luminal Bridge";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
