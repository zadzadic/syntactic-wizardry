package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LuminalBridgeStructure {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 9;
    private static final int SEARCH_RADIUS = 11;

    public record PortalGeometry(
            Direction front,
            BlockPos min,
            BlockPos max,
            List<BlockPos> portalPositions,
            List<BlockPos> borderPositions,
            List<BlockPos> backingPositions) {
        public PortalGeometry {
            portalPositions = List.copyOf(portalPositions);
            borderPositions = List.copyOf(borderPositions);
            backingPositions = List.copyOf(backingPositions);
        }

        public int width() {
            return switch (front.getAxis()) {
                case X -> max.getZ() - min.getZ() + 1;
                case Y, Z -> max.getX() - min.getX() + 1;
            };
        }

        public int height() {
            return switch (front.getAxis()) {
                case X, Z -> max.getY() - min.getY() + 1;
                case Y -> max.getZ() - min.getZ() + 1;
            };
        }

        public BlockPos centerBlock() {
            return new BlockPos(
                    (min.getX() + max.getX()) >> 1,
                    (min.getY() + max.getY()) >> 1,
                    (min.getZ() + max.getZ()) >> 1);
        }

        public boolean contains(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }
    }

    public record Detection(boolean valid, String error, PortalGeometry geometry) {}

    private LuminalBridgeStructure() {}

    public static Detection detect(ServerLevel level, BlockPos center) {
        if (level == null || center == null || !RitualStructureRules.isValidCenter(level.getBlockState(center))) {
            return new Detection(false, "The Center must be a Mature Crystal.", null);
        }

        if (ReliquaryRegistry.block() == null || !level.getBlockState(center.below()).is(ReliquaryRegistry.block())) {
            return new Detection(false, "Luminal Bridge requires a Reliquary directly below the Mature Crystal.", null);
        }

        List<PortalGeometry> candidates = findCandidates(level, center);
        if (candidates.isEmpty()) {
            return new Detection(false,
                    "Luminal Bridge requires a rectangular 1x1 to 9x9 Rune aperture with a complete Structural border and Structural backing.",
                    null);
        }

        PortalGeometry geometry = candidates.stream()
                .min(Comparator
                        .comparingDouble((PortalGeometry g) -> g.centerBlock().distSqr(center))
                        .thenComparingInt(g -> -g.portalPositions().size()))
                .orElse(null);
        return geometry == null
                ? new Detection(false, "The Luminal Bridge aperture is incomplete.", null)
                : new Detection(true, "", geometry);
    }

    public static boolean activeStructureValid(ServerLevel level, BlockPos center, PortalGeometry geometry) {
        if (level == null || center == null || geometry == null) return false;
        if (ReliquaryRegistry.block() == null || !level.getBlockState(center.below()).is(ReliquaryRegistry.block())) return false;
        if (!validateBorderAndBacking(level, geometry)) return false;
        if (LuminalPortalRegistry.block() == null) return false;
        for (BlockPos pos : geometry.portalPositions()) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(LuminalPortalRegistry.block()) && !RitualStructureRules.isValidRune(state)) return false;
        }
        return true;
    }

    public static PortalGeometry geometry(
            Direction front,
            BlockPos min,
            BlockPos max,
            List<BlockPos> portalPositions) {
        return buildGeometry(front, min, max, portalPositions);
    }

    private static List<PortalGeometry> findCandidates(ServerLevel level, BlockPos center) {
        List<PortalGeometry> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int x = center.getX() - SEARCH_RADIUS; x <= center.getX() + SEARCH_RADIUS; x++) {
            for (int y = center.getY() - SEARCH_RADIUS; y <= center.getY() + SEARCH_RADIUS; y++) {
                for (int z = center.getZ() - SEARCH_RADIUS; z <= center.getZ() + SEARCH_RADIUS; z++) {
                    BlockPos seed = new BlockPos(x, y, z);
                    if (!RitualStructureRules.isValidRune(level.getBlockState(seed))) continue;

                    for (Direction.Axis normalAxis : Direction.Axis.values()) {
                        Component component = component(level, seed, normalAxis);
                        if (component.positions.isEmpty()) continue;
                        if (component.width < MIN_SIZE || component.width > MAX_SIZE
                                || component.height < MIN_SIZE || component.height > MAX_SIZE) continue;
                        if (component.positions.size() != component.width * component.height) continue;
                        if (!rectangleFilledWithRunes(level, component, normalAxis)) continue;

                        for (Direction front : directionsForAxis(normalAxis)) {
                            PortalGeometry geometry = buildGeometry(front, component.min, component.max, component.positions);
                            String key = front.getName() + ":" + geometry.min().asLong() + ":" + geometry.max().asLong();
                            if (!seen.add(key)) continue;
                            if (validateBorderAndBacking(level, geometry)) result.add(geometry);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean rectangleFilledWithRunes(ServerLevel level, Component c, Direction.Axis normalAxis) {
        for (BlockPos pos : rectanglePositions(normalAxis, c.min, c.max)) {
            if (!RitualStructureRules.isValidRune(level.getBlockState(pos))) return false;
        }
        return true;
    }

    private static boolean validateBorderAndBacking(ServerLevel level, PortalGeometry geometry) {
        for (BlockPos pos : geometry.borderPositions()) {
            if (!RitualStructureRules.isValidStructural(level, pos)) return false;
        }
        for (BlockPos pos : geometry.backingPositions()) {
            if (!RitualStructureRules.isValidStructural(level, pos)) return false;
        }
        return true;
    }

    private static PortalGeometry buildGeometry(Direction front, BlockPos min, BlockPos max, List<BlockPos> portalPositions) {
        Direction.Axis axis = front.getAxis();
        List<BlockPos> border = borderPositions(axis, min, max);
        Direction back = front.getOpposite();
        List<BlockPos> backing = new ArrayList<>();
        for (BlockPos pos : portalPositions) backing.add(pos.relative(back));
        for (BlockPos pos : border) backing.add(pos.relative(back));
        return new PortalGeometry(front, min.immutable(), max.immutable(), portalPositions, border, distinct(backing));
    }

    private static List<BlockPos> borderPositions(Direction.Axis axis, BlockPos min, BlockPos max) {
        List<BlockPos> result = new ArrayList<>();
        switch (axis) {
            case X -> {
                int x = min.getX();
                for (int y = min.getY() - 1; y <= max.getY() + 1; y++) {
                    for (int z = min.getZ() - 1; z <= max.getZ() + 1; z++) {
                        if (y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ()) continue;
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
            case Y -> {
                int y = min.getY();
                for (int x = min.getX() - 1; x <= max.getX() + 1; x++) {
                    for (int z = min.getZ() - 1; z <= max.getZ() + 1; z++) {
                        if (x >= min.getX() && x <= max.getX() && z >= min.getZ() && z <= max.getZ()) continue;
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
            case Z -> {
                int z = min.getZ();
                for (int x = min.getX() - 1; x <= max.getX() + 1; x++) {
                    for (int y = min.getY() - 1; y <= max.getY() + 1; y++) {
                        if (x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY()) continue;
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return List.copyOf(result);
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

    private static Component component(ServerLevel level, BlockPos seed, Direction.Axis normalAxis) {
        if (!RitualStructureRules.isValidRune(level.getBlockState(seed))) return Component.empty();
        int plane = axisValue(seed, normalAxis);
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        found.add(seed.immutable());

        while (!queue.isEmpty() && found.size() <= MAX_SIZE * MAX_SIZE) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : planarDirections(normalAxis)) {
                BlockPos next = current.relative(direction);
                if (axisValue(next, normalAxis) != plane || found.contains(next)) continue;
                if (!RitualStructureRules.isValidRune(level.getBlockState(next))) continue;
                found.add(next.immutable());
                queue.add(next);
            }
        }

        if (found.isEmpty() || found.size() > MAX_SIZE * MAX_SIZE) return Component.empty();

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : found) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        BlockPos min = new BlockPos(minX, minY, minZ);
        BlockPos max = new BlockPos(maxX, maxY, maxZ);
        int width;
        int height;
        switch (normalAxis) {
            case X -> {
                width = maxZ - minZ + 1;
                height = maxY - minY + 1;
            }
            case Y -> {
                width = maxX - minX + 1;
                height = maxZ - minZ + 1;
            }
            case Z -> {
                width = maxX - minX + 1;
                height = maxY - minY + 1;
            }
            default -> throw new IllegalStateException();
        }
        return new Component(List.copyOf(found), min, max, width, height);
    }

    private static int axisValue(BlockPos pos, Direction.Axis axis) {
        return switch (axis) {
            case X -> pos.getX();
            case Y -> pos.getY();
            case Z -> pos.getZ();
        };
    }

    private static Direction[] planarDirections(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
            case Y -> new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
            case Z -> new Direction[]{Direction.UP, Direction.DOWN, Direction.WEST, Direction.EAST};
        };
    }

    private static Direction[] directionsForAxis(Direction.Axis axis) {
        return switch (axis) {
            case X -> new Direction[]{Direction.EAST, Direction.WEST};
            case Y -> new Direction[]{Direction.UP, Direction.DOWN};
            case Z -> new Direction[]{Direction.SOUTH, Direction.NORTH};
        };
    }

    private static List<BlockPos> distinct(List<BlockPos> values) {
        return List.copyOf(new HashSet<>(values));
    }

    private record Component(List<BlockPos> positions, BlockPos min, BlockPos max, int width, int height) {
        static Component empty() {
            return new Component(List.of(), BlockPos.ZERO, BlockPos.ZERO, 0, 0);
        }
    }
}
