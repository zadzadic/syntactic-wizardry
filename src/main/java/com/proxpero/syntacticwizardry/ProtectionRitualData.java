package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ProtectionRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_protection_rituals_v1";
    public static final double DEFAULT_RADIUS = 20.0D;
    private static final double QUERY_MARGIN = 4.0D;
    private static final int BORDER_INTERVAL_TICKS = 5;
    private static final int BORDER_SAMPLES = 96;

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private final UUID ownerId;
        private final boolean customArea;
        private final ProtectionAreaShape shape;
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private String name;
        private int effectTicks;
        private boolean paused;
        private boolean powered;
        private boolean stopping;

        private final transient Map<UUID, Vec3> entityPositions = new HashMap<>();
        private final transient Map<UUID, Vec3> projectilePositions = new HashMap<>();

        private Entry(UUID id, BlockPos center, UUID ownerId, boolean customArea,
                      ProtectionAreaShape shape,
                      int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                      String name, int effectTicks, boolean paused, boolean powered, boolean stopping) {
            this.id = id;
            this.center = center.immutable();
            this.ownerId = ownerId;
            this.customArea = customArea;
            this.shape = shape == null ? ProtectionAreaShape.SPHERE : shape;
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
            this.name = sanitizeName(name);
            this.effectTicks = RitualTransitionRules.clampTicks(effectTicks);
            this.paused = paused;
            this.powered = powered;
            this.stopping = stopping;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public UUID ownerId() { return ownerId; }
        public boolean customArea() { return customArea; }
        public ProtectionAreaShape shape() { return shape; }
        public int minX() { return minX; }
        public int minY() { return minY; }
        public int minZ() { return minZ; }
        public int maxX() { return maxX; }
        public int maxY() { return maxY; }
        public int maxZ() { return maxZ; }
        public String name() { return name; }
        public int effectTicks() { return effectTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public boolean stopping() { return stopping; }

        public boolean targetActive() {
            return !paused && !stopping && powered;
        }

        private double centerX() {
            return customArea ? (minX + maxX + 1.0D) * 0.5D : center.getX() + 0.5D;
        }

        private double centerY() {
            return customArea ? (minY + maxY + 1.0D) * 0.5D : center.getY() + 0.5D;
        }

        private double centerZ() {
            return customArea ? (minZ + maxZ + 1.0D) * 0.5D : center.getZ() + 0.5D;
        }

        private double halfX() {
            return customArea ? (maxX - minX + 1.0D) * 0.5D : DEFAULT_RADIUS;
        }

        private double halfY() {
            return customArea ? (maxY - minY + 1.0D) * 0.5D : DEFAULT_RADIUS;
        }

        private double halfZ() {
            return customArea ? (maxZ - minZ + 1.0D) * 0.5D : DEFAULT_RADIUS;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private ProtectionRitualData() {}

    public static Factory<ProtectionRitualData> factory() {
        return new Factory<>(ProtectionRitualData::new, ProtectionRitualData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static ProtectionRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static ProtectionRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        ProtectionRitualData data = new ProtectionRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id") || !ritualTag.hasUUID("Owner")) continue;
            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
                    ritualTag.getUUID("Owner"),
                    ritualTag.getBoolean("CustomArea"),
                    ProtectionAreaShape.fromOrdinal(ritualTag.getInt("Shape")),
                    ritualTag.getInt("MinX"), ritualTag.getInt("MinY"), ritualTag.getInt("MinZ"),
                    ritualTag.getInt("MaxX"), ritualTag.getInt("MaxY"), ritualTag.getInt("MaxZ"),
                    ritualTag.getString("Name"),
                    ritualTag.getInt("EffectTicks"),
                    ritualTag.getBoolean("Paused"),
                    ritualTag.getBoolean("Powered"),
                    ritualTag.getBoolean("Stopping")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag ritualTag = new CompoundTag();
            ritualTag.putUUID("Id", entry.id);
            ritualTag.putLong("Center", entry.center.asLong());
            ritualTag.putUUID("Owner", entry.ownerId);
            ritualTag.putBoolean("CustomArea", entry.customArea);
            ritualTag.putInt("Shape", entry.shape.ordinal());
            ritualTag.putInt("MinX", entry.minX);
            ritualTag.putInt("MinY", entry.minY);
            ritualTag.putInt("MinZ", entry.minZ);
            ritualTag.putInt("MaxX", entry.maxX);
            ritualTag.putInt("MaxY", entry.maxY);
            ritualTag.putInt("MaxZ", entry.maxZ);
            ritualTag.putString("Name", entry.name);
            ritualTag.putInt("EffectTicks", entry.effectTicks);
            ritualTag.putBoolean("Paused", entry.paused);
            ritualTag.putBoolean("Powered", entry.powered);
            ritualTag.putBoolean("Stopping", entry.stopping);
            list.add(ritualTag);
        }
        tag.put("Rituals", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry activate(ServerLevel level, BlockPos center, UUID ownerId,
                          ProtectionPreparedAreaData.Config prepared) {
        boolean custom = prepared != null;
        ProtectionAreaShape shape = custom ? prepared.shape() : ProtectionAreaShape.SPHERE;
        int minX = custom ? prepared.minX() : center.getX() - 20;
        int minY = custom ? prepared.minY() : center.getY() - 20;
        int minZ = custom ? prepared.minZ() : center.getZ() - 20;
        int maxX = custom ? prepared.maxX() : center.getX() + 19;
        int maxY = custom ? prepared.maxY() : center.getY() + 19;
        int maxZ = custom ? prepared.maxZ() : center.getZ() + 19;

        Entry entry = new Entry(
                UUID.randomUUID(), center, ownerId, custom, shape,
                minX, minY, minZ, maxX, maxY, maxZ,
                "Protection", 0, false,
                RitualManaSupport.tryConsumeUpkeep(level, center, 1.0D), false);
        entries.add(entry);
        setDirty();
        return entry;
    }

    public boolean tick(ServerLevel level) {
        boolean syncChanged = false;
        boolean dirty = false;
        long time = level.getGameTime();

        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();

            if (!entry.stopping && !ProtectionRitualStructure.activeStructureValid(level, entry.center)) {
                entry.stopping = true;
                entry.paused = false;
                entry.powered = false;
                syncChanged = true;
                dirty = true;
            }

            if (entry.stopping || entry.paused) {
                if (entry.powered) {
                    entry.powered = false;
                    syncChanged = true;
                    dirty = true;
                }
            } else if (time % 20L == 0L) {
                boolean powered = RitualManaSupport.tryConsumeUpkeep(level, entry.center, 1.0D);
                if (powered != entry.powered) {
                    entry.powered = powered;
                    syncChanged = true;
                    dirty = true;
                }
            }

            int previousTicks = entry.effectTicks;
            entry.effectTicks = RitualTransitionRules.step(entry.effectTicks, entry.targetActive());
            if (entry.effectTicks != previousTicks) dirty = true;

            float progress = RitualTransitionRules.progress(entry.effectTicks);
            if (progress > 0.001F) {
                enforceBoundary(level, entry, progress);
                if (time % BORDER_INTERVAL_TICKS == 0L) renderBorder(level, entry, progress);
            } else {
                entry.entityPositions.clear();
                entry.projectilePositions.clear();
            }

            if (entry.stopping && entry.effectTicks <= 0) {
                iterator.remove();
                syncChanged = true;
                dirty = true;
            }
        }

        if (dirty) setDirty();
        return syncChanged;
    }

    private static void enforceBoundary(ServerLevel level, Entry entry, float progress) {
        AABB query = queryBounds(entry, progress).inflate(QUERY_MARGIN);
        BoundCreatureData bindings = BoundCreatureData.get(level.getServer());

        Set<UUID> seenEntities = new HashSet<>();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, query,
                candidate -> candidate != null && !candidate.isRemoved() && !(candidate instanceof Projectile))) {
            UUID id = entity.getUUID();
            seenEntities.add(id);
            Vec3 current = new Vec3(entity.getX(), entity.getY(), entity.getZ());
            Vec3 previous = entry.entityPositions.put(id, current);
            if (previous == null) previous = current.subtract(entity.getDeltaMovement());

            if (bindings.isBound(id)) continue;
            if (inside(entry, previous, progress) == inside(entry, current, progress)) continue;

            entity.teleportTo(previous.x, previous.y, previous.z);
            entity.setDeltaMovement(Vec3.ZERO);
            entry.entityPositions.put(id, previous);
        }
        entry.entityPositions.keySet().retainAll(seenEntities);

        Set<UUID> seenProjectiles = new HashSet<>();
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, query,
                candidate -> candidate != null && !candidate.isRemoved())) {
            UUID id = projectile.getUUID();
            seenProjectiles.add(id);
            Vec3 current = new Vec3(projectile.getX(), projectile.getY(), projectile.getZ());
            Vec3 previous = entry.projectilePositions.put(id, current);
            if (previous == null) previous = current.subtract(projectile.getDeltaMovement());

            if (inside(entry, previous, progress) == inside(entry, current, progress)) continue;

            double damage = projectileDamage(projectile);
            if (RitualManaSupport.tryConsumeUpkeep(level, entry.center, damage)) {
                projectile.discard();
                entry.projectilePositions.remove(id);
            }
        }
        entry.projectilePositions.keySet().retainAll(seenProjectiles);
    }

    private static boolean inside(Entry entry, Vec3 point, float progress) {
        double scale = Math.max(0.001D, progress);
        double dx = point.x - entry.centerX();
        double dy = point.y - entry.centerY();
        double dz = point.z - entry.centerZ();

        double hx = Math.max(0.001D, entry.halfX() * scale);
        double hy = Math.max(0.001D, entry.halfY() * scale);
        double hz = Math.max(0.001D, entry.halfZ() * scale);

        return switch (entry.shape) {
            case BOX -> Math.abs(dx) <= hx && Math.abs(dy) <= hy && Math.abs(dz) <= hz;
            case CYLINDER -> (dx * dx) / (hx * hx) + (dz * dz) / (hz * hz) <= 1.0D
                    && Math.abs(dy) <= hy;
            case SPHERE -> (dx * dx) / (hx * hx) + (dy * dy) / (hy * hy) + (dz * dz) / (hz * hz) <= 1.0D;
        };
    }

    private static AABB queryBounds(Entry entry, float progress) {
        double scale = Math.max(0.001D, progress);
        double hx = entry.halfX() * scale;
        double hy = entry.halfY() * scale;
        double hz = entry.halfZ() * scale;
        return new AABB(
                entry.centerX() - hx, entry.centerY() - hy, entry.centerZ() - hz,
                entry.centerX() + hx, entry.centerY() + hy, entry.centerZ() + hz);
    }

    private static double projectileDamage(Projectile projectile) {
        if (projectile instanceof AbstractArrow arrow) {
            double speed = arrow.getDeltaMovement().length();
            return Math.max(0.0D, Math.ceil(arrow.getBaseDamage() * speed));
        }

        try {
            Method method = projectile.getClass().getMethod("getBaseDamage");
            Object value = method.invoke(projectile);
            if (value instanceof Number number) return Math.max(0.0D, number.doubleValue());
        } catch (ReflectiveOperationException ignored) {
        }

        String name = projectile.getClass().getSimpleName();
        if (name.contains("ShulkerBullet")) return 4.0D;
        if (name.contains("SmallFireball")) return 5.0D;
        if (name.contains("LargeFireball")) return 6.0D;
        if (name.contains("WitherSkull")) return 8.0D;
        if (name.contains("LlamaSpit")) return 1.0D;
        if (name.contains("DragonFireball")) return 6.0D;
        if (name.contains("Snowball") || name.contains("Egg")) return 0.0D;
        return 1.0D;
    }

    private static void renderBorder(ServerLevel level, Entry entry, float progress) {
        double scale = Math.max(0.001D, progress);
        double hx = entry.halfX() * scale;
        double hz = entry.halfZ() * scale;
        double cx = entry.centerX();
        double cz = entry.centerZ();

        if (entry.shape == ProtectionAreaShape.BOX) {
            int perSide = Math.max(8, BORDER_SAMPLES / 4);
            for (int i = 0; i < perSide; i++) {
                double t = i / (double) perSide;
                borderParticle(level, entry, cx - hx + 2.0D * hx * t, cz - hz);
                borderParticle(level, entry, cx - hx + 2.0D * hx * t, cz + hz);
                borderParticle(level, entry, cx - hx, cz - hz + 2.0D * hz * t);
                borderParticle(level, entry, cx + hx, cz - hz + 2.0D * hz * t);
            }
            return;
        }

        for (int i = 0; i < BORDER_SAMPLES; i++) {
            double angle = Math.PI * 2.0D * i / BORDER_SAMPLES;
            double x = cx + Math.cos(angle) * hx;
            double z = cz + Math.sin(angle) * hz;
            borderParticle(level, entry, x, z);
        }
    }

    private static void borderParticle(ServerLevel level, Entry entry, double x, double z) {
        double y = groundY(level, entry, x, z);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static double groundY(ServerLevel level, Entry entry, double x, double z) {
        int bx = (int)Math.floor(x);
        int bz = (int)Math.floor(z);
        int centerY = (int)Math.floor(entry.centerY());

        for (int y = centerY + 8; y >= centerY - 12; y--) {
            BlockPos pos = new BlockPos(bx, y, bz);
            BlockPos above = pos.above();
            if (!level.getBlockState(pos).isAir()
                    && level.getBlockState(pos).isSolidRender(level, pos)
                    && level.getBlockState(above).isAir()) {
                return y + 1.04D;
            }
        }
        return entry.centerY() - 1.0D;
    }

    public Entry find(UUID id) {
        if (id == null) return null;
        for (Entry entry : entries) if (entry.id.equals(id)) return entry;
        return null;
    }

    public boolean setPaused(UUID id, boolean paused) {
        Entry entry = find(id);
        if (entry == null || entry.stopping || entry.paused == paused) return false;
        entry.paused = paused;
        if (paused) entry.powered = false;
        setDirty();
        return true;
    }

    public boolean rename(UUID id, String name) {
        Entry entry = find(id);
        if (entry == null) return false;
        String sanitized = sanitizeName(name);
        if (entry.name.equals(sanitized)) return false;
        entry.name = sanitized;
        setDirty();
        return true;
    }

    public boolean stop(UUID id) {
        Entry entry = find(id);
        if (entry == null || entry.stopping) return false;
        if (entry.effectTicks <= 0) {
            entries.remove(entry);
        } else {
            entry.stopping = true;
            entry.paused = false;
            entry.powered = false;
        }
        setDirty();
        return true;
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Protection";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}