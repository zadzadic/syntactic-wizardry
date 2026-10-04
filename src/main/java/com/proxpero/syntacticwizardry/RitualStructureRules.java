package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared physical rules for every ritual structure.
 *
 * Individual ritual definitions provide their Rune and Structural positions.
 * These common rules define the Center, reserved Focus band, Focus materials,
 * Focus Potence, and generic role validation.
 */
public final class RitualStructureRules {
    public enum Role {
        CENTER,
        RUNE,
        STRUCTURAL,
        FOCUS,
        SPECIFIC
    }

    /**
     * A ritual-relative block requirement.
     *
     * CENTER/RUNE/STRUCTURAL/FOCUS use the shared role rules. SPECIFIC requires
     * the exact supplied Block identity but ignores that block's state properties.
     */
    public record PatternSlot(BlockPos offset, Role role, Supplier<? extends Block> specificBlock) {
        public PatternSlot {
            if (offset == null || role == null) throw new IllegalArgumentException("Ritual pattern slots require an offset and role.");
            if (role == Role.SPECIFIC && specificBlock == null) {
                throw new IllegalArgumentException("Specific ritual slots require a block supplier.");
            }
        }

        public PatternSlot(BlockPos offset, Role role) {
            this(offset, role, null);
        }

        public static PatternSlot specific(BlockPos offset, Block block) {
            if (block == null) throw new IllegalArgumentException("Specific ritual slots require a block.");
            return new PatternSlot(offset, Role.SPECIFIC, () -> block);
        }

        public static PatternSlot specific(BlockPos offset, Supplier<? extends Block> blockSupplier) {
            return new PatternSlot(offset, Role.SPECIFIC, blockSupplier);
        }

        public BlockPos position(BlockPos center) {
            return center.offset(offset);
        }
    }

    /** A valid Focus discovered in a ritual focus slot. */
    public record MatchedFocus(BlockPos position, FocusMaterial material) {}

    /** Result of the shared ritual block detector. */
    public record PatternMatch(
            boolean valid,
            Role failedRole,
            BlockPos failedPosition,
            List<MatchedFocus> foci) {
        public PatternMatch {
            foci = List.copyOf(foci == null ? List.of() : foci);
        }
    }

    public enum FocusMaterial {
        IRON("Iron", 1, 4),
        GOLD("Gold", 2, 6),
        EMERALD("Emerald", 3, 8),
        DIAMOND("Diamond", 4, Integer.MAX_VALUE);

        private final String displayName;
        private final int value;
        private final int contributionCap;

        FocusMaterial(String displayName, int value, int contributionCap) {
            this.displayName = displayName;
            this.value = value;
            this.contributionCap = contributionCap;
        }

        public String displayName() {
            return displayName;
        }

        public int value() {
            return value;
        }

        public int contributionCap() {
            return contributionCap;
        }

        public boolean matches(BlockState state) {
            return switch (this) {
                case IRON -> state.is(Blocks.IRON_BLOCK);
                case GOLD -> state.is(Blocks.GOLD_BLOCK);
                case EMERALD -> state.is(Blocks.EMERALD_BLOCK);
                case DIAMOND -> state.is(Blocks.DIAMOND_BLOCK);
            };
        }
    }

    public record FocusPlacement(BlockPos offset, FocusMaterial material) {
        public BlockPos position(BlockPos center) {
            return center.offset(offset);
        }
    }

    public record FocusPlan(int requestedPotence, int suppliedPotence, List<FocusPlacement> placements) {
        public FocusPlan {
            placements = List.copyOf(placements);
        }

        public String summary() {
            int iron = 0;
            int gold = 0;
            int emerald = 0;
            int diamond = 0;
            for (FocusPlacement placement : placements) {
                switch (placement.material()) {
                    case IRON -> iron++;
                    case GOLD -> gold++;
                    case EMERALD -> emerald++;
                    case DIAMOND -> diamond++;
                }
            }

            List<String> parts = new ArrayList<>();
            if (diamond > 0) parts.add(diamond + " Diamond");
            if (emerald > 0) parts.add(emerald + " Emerald");
            if (gold > 0) parts.add(gold + " Gold");
            if (iron > 0) parts.add(iron + " Iron");
            return String.join(" + ", parts);
        }
    }

    private record MaterialCounts(int iron, int gold, int emerald, int diamond) {
        int blocks() {
            return iron + gold + emerald + diamond;
        }

        int supplied() {
            int ironPower = Math.min(iron * FocusMaterial.IRON.value(), FocusMaterial.IRON.contributionCap());
            int goldPower = Math.min(gold * FocusMaterial.GOLD.value(), FocusMaterial.GOLD.contributionCap());
            int emeraldPower = Math.min(emerald * FocusMaterial.EMERALD.value(), FocusMaterial.EMERALD.contributionCap());
            int diamondPower = diamond * FocusMaterial.DIAMOND.value();
            return ironPower + goldPower + emeraldPower + diamondPower;
        }

        int materialRankCost() {
            return iron + gold * 10 + emerald * 100 + diamond * 1000;
        }
    }

    public static final double FOCUS_DISTANCE = 3.0D;
    public static final double FOCUS_BAND_MIN = 2.5D;
    public static final double FOCUS_BAND_MAX = 3.5D;
    public static final double EFFECT_ORIGIN_MAX_DISTANCE = 5.0D;
    public static final int MAX_FOCUS_SLOTS = 8;

    private RitualStructureRules() {}

    public static boolean isValidCenter(BlockState state) {
        return state.is(SyntacticWizardry.MATURE_CRYSTAL.get());
    }

    public static boolean isValidRune(BlockState state) {
        return ChalkRegistry.block() != null && state.is(ChalkRegistry.block());
    }

    public static boolean isValidStructural(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.isSolidRender(level, pos)) return false;
        for (Direction direction : Direction.values()) {
            if (state.getFlammability(level, pos, direction) > 0) return false;
        }
        return true;
    }

    public static boolean isValidFocus(BlockState state) {
        return focusMaterial(state) != null;
    }

    public static boolean isValidFocus(BlockState state, FocusMaterial material) {
        return material != null && material.matches(state);
    }

    /**
     * Generic ritual role validation.
     *
     * Runes are accepted regardless of glyph, color, or facing.
     * Structural blocks are accepted regardless of material, provided they satisfy
     * the shared structural-block rule.
     * Focuses are accepted when they are any registered valid Focus material.
     */
    public static boolean isValidForRole(LevelReader level, BlockPos pos, Role role) {
        BlockState state = level.getBlockState(pos);
        return switch (role) {
            case CENTER -> isValidCenter(state);
            case RUNE -> isValidRune(state);
            case STRUCTURAL -> isValidStructural(level, pos);
            case FOCUS -> isValidFocus(state);
            case SPECIFIC -> false;
        };
    }

    public static boolean isValidForSlot(LevelReader level, BlockPos center, PatternSlot slot) {
        if (level == null || center == null || slot == null) return false;
        BlockPos pos = slot.position(center);
        if (slot.role() != Role.SPECIFIC) return isValidForRole(level, pos, slot.role());
        Block expected = slot.specificBlock() == null ? null : slot.specificBlock().get();
        return expected != null && level.getBlockState(pos).is(expected);
    }

    /** Backward-compatible exact-Focus overload for UI/planning code. */
    public static boolean isValidForRole(
            LevelReader level,
            BlockPos pos,
            Role role,
            FocusMaterial focusMaterial) {
        if (role != Role.FOCUS) return isValidForRole(level, pos, role);
        return isValidFocus(level.getBlockState(pos), focusMaterial);
    }

    /**
     * Shared detector used by every ritual structure.
     *
     * Required slots are validated only by their role. Optional focus positions are
     * scanned using the same generic Focus rule, and minFocusCount controls whether
     * the ritual requires Focuses at all.
     */
    public static PatternMatch detectPattern(
            LevelReader level,
            BlockPos center,
            List<PatternSlot> requiredSlots,
            List<BlockPos> focusOffsets,
            int minFocusCount) {
        if (level == null || center == null) {
            return new PatternMatch(false, Role.CENTER, center, List.of());
        }

        List<PatternSlot> slots = requiredSlots == null ? List.of() : requiredSlots;
        for (PatternSlot slot : slots) {
            BlockPos pos = slot.position(center);
            if (!isValidForSlot(level, center, slot)) {
                return new PatternMatch(false, slot.role(), pos.immutable(), List.of());
            }
        }

        List<MatchedFocus> foci = new ArrayList<>();
        List<BlockPos> candidates = focusOffsets == null ? List.of() : focusOffsets;
        for (BlockPos offset : candidates) {
            if (offset == null) continue;
            BlockPos pos = center.offset(offset);
            FocusMaterial material = focusMaterial(level.getBlockState(pos));
            if (material != null) foci.add(new MatchedFocus(pos.immutable(), material));
        }

        if (foci.size() < Math.max(0, minFocusCount)) {
            return new PatternMatch(false, Role.FOCUS, center.immutable(), foci);
        }
        return new PatternMatch(true, null, null, foci);
    }

    public static PatternMatch detectPattern(
            LevelReader level,
            BlockPos center,
            List<PatternSlot> requiredSlots) {
        return detectPattern(level, center, requiredSlots, List.of(), 0);
    }

    /** Counts how many required ritual slots currently satisfy their generic role. */
    public static int countPatternMatches(LevelReader level, BlockPos center, List<PatternSlot> requiredSlots) {
        if (level == null || center == null || requiredSlots == null) return 0;
        int matches = 0;
        for (PatternSlot slot : requiredSlots) {
            if (slot != null && isValidForSlot(level, center, slot)) matches++;
        }
        return matches;
    }

    public static boolean isReservedFocusBand(BlockPos center, BlockPos pos) {
        if (center == null || pos == null) return false;
        double dx = pos.getX() - center.getX();
        double dy = pos.getY() - center.getY();
        double dz = pos.getZ() - center.getZ();
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq >= FOCUS_BAND_MIN * FOCUS_BAND_MIN
                && distanceSq <= FOCUS_BAND_MAX * FOCUS_BAND_MAX;
    }

    public static boolean isEffectOriginAllowed(BlockPos center, BlockPos effectOrigin) {
        if (center == null || effectOrigin == null) return false;
        return center.distSqr(effectOrigin) <= EFFECT_ORIGIN_MAX_DISTANCE * EFFECT_ORIGIN_MAX_DISTANCE;
    }

    /**
     * Generates the default material plan.
     *
     * The tier required by the requested Potence is also the highest tier the
     * automatic plan will use. This avoids spending Diamond on a Potence-6
     * ritual when Gold is the intended tier.
     */
    public static FocusPlan focusPlan(int requestedPotence) {
        int target = Math.max(1, requestedPotence);
        FocusMaterial requiredTier = requiredTier(target);

        List<MaterialCounts> candidates = new ArrayList<>();
        int maxEach = MAX_FOCUS_SLOTS;
        for (int diamond = 0; diamond <= maxEach; diamond++) {
            for (int emerald = 0; emerald <= maxEach - diamond; emerald++) {
                for (int gold = 0; gold <= maxEach - diamond - emerald; gold++) {
                    for (int iron = 0; iron <= maxEach - diamond - emerald - gold; iron++) {
                        MaterialCounts counts = new MaterialCounts(iron, gold, emerald, diamond);
                        if (counts.blocks() == 0 || counts.supplied() < target) continue;
                        if (!usesRequiredTier(counts, requiredTier)) continue;
                        if (usesTierAbove(counts, requiredTier)) continue;
                        candidates.add(counts);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            int diamonds = Math.max(1, (target + FocusMaterial.DIAMOND.value() - 1) / FocusMaterial.DIAMOND.value());
            diamonds = Math.min(diamonds, MAX_FOCUS_SLOTS);
            MaterialCounts fallback = new MaterialCounts(0, 0, 0, diamonds);
            return buildPlan(target, fallback);
        }

        candidates.sort(Comparator
                .comparingInt((MaterialCounts counts) -> counts.supplied() - target)
                .thenComparingInt(MaterialCounts::blocks)
                .thenComparingInt(MaterialCounts::materialRankCost));

        return buildPlan(target, candidates.get(0));
    }

    public static List<BlockPos> focusSlots() {
        return List.of(
                new BlockPos(3, 0, 0),
                new BlockPos(2, 0, 2),
                new BlockPos(0, 0, 3),
                new BlockPos(-2, 0, 2),
                new BlockPos(-3, 0, 0),
                new BlockPos(-2, 0, -2),
                new BlockPos(0, 0, -3),
                new BlockPos(2, 0, -2));
    }

    public static FocusMaterial focusMaterial(BlockState state) {
        if (state == null) return null;
        for (FocusMaterial material : FocusMaterial.values()) {
            if (material.matches(state)) return material;
        }
        return null;
    }

    public static FocusMaterial requiredTier(int requestedPotence) {
        int target = Math.max(1, requestedPotence);
        if (target <= 4) return FocusMaterial.IRON;
        if (target <= 6) return FocusMaterial.GOLD;
        if (target <= 8) return FocusMaterial.EMERALD;
        return FocusMaterial.DIAMOND;
    }

    public static int suppliedPotence(List<BlockState> focusStates) {
        if (focusStates == null || focusStates.isEmpty()) return 0;
        int iron = 0;
        int gold = 0;
        int emerald = 0;
        int diamond = 0;
        for (BlockState state : focusStates) {
            if (state.is(Blocks.IRON_BLOCK)) iron++;
            else if (state.is(Blocks.GOLD_BLOCK)) gold++;
            else if (state.is(Blocks.EMERALD_BLOCK)) emerald++;
            else if (state.is(Blocks.DIAMOND_BLOCK)) diamond++;
        }
        return new MaterialCounts(iron, gold, emerald, diamond).supplied();
    }

    private static FocusPlan buildPlan(int target, MaterialCounts counts) {
        List<FocusMaterial> materials = new ArrayList<>();
        for (int i = 0; i < counts.diamond(); i++) materials.add(FocusMaterial.DIAMOND);
        for (int i = 0; i < counts.emerald(); i++) materials.add(FocusMaterial.EMERALD);
        for (int i = 0; i < counts.gold(); i++) materials.add(FocusMaterial.GOLD);
        for (int i = 0; i < counts.iron(); i++) materials.add(FocusMaterial.IRON);

        List<BlockPos> offsets = focusOffsets(materials.size());
        List<FocusPlacement> placements = new ArrayList<>();
        for (int i = 0; i < Math.min(materials.size(), offsets.size()); i++) {
            placements.add(new FocusPlacement(offsets.get(i), materials.get(i)));
        }
        return new FocusPlan(target, counts.supplied(), placements);
    }

    private static List<BlockPos> focusOffsets(int count) {
        if (count <= 0) return List.of();
        if (count == 1) return List.of(new BlockPos(3, 0, 0));
        if (count == 2) return List.of(
                new BlockPos(-3, 0, 0),
                new BlockPos(3, 0, 0));
        if (count == 3) return List.of(
                new BlockPos(0, 0, -3),
                new BlockPos(-2, 0, 2),
                new BlockPos(2, 0, 2));
        if (count == 4) return List.of(
                new BlockPos(-3, 0, 0),
                new BlockPos(0, 0, -3),
                new BlockPos(3, 0, 0),
                new BlockPos(0, 0, 3));

        List<BlockPos> ring = List.of(
                new BlockPos(3, 0, 0),
                new BlockPos(2, 0, 2),
                new BlockPos(0, 0, 3),
                new BlockPos(-2, 0, 2),
                new BlockPos(-3, 0, 0),
                new BlockPos(-2, 0, -2),
                new BlockPos(0, 0, -3),
                new BlockPos(2, 0, -2));

        int used = Math.min(count, ring.size());
        List<BlockPos> result = new ArrayList<>(used);
        for (int i = 0; i < used; i++) {
            int index = Math.floorMod((int)Math.round(i * (ring.size() / (double)used)), ring.size());
            result.add(ring.get(index));
        }
        return Collections.unmodifiableList(result);
    }

    private static boolean usesRequiredTier(MaterialCounts counts, FocusMaterial tier) {
        return switch (tier) {
            case IRON -> counts.iron() > 0;
            case GOLD -> counts.gold() > 0;
            case EMERALD -> counts.emerald() > 0;
            case DIAMOND -> counts.diamond() > 0;
        };
    }

    private static boolean usesTierAbove(MaterialCounts counts, FocusMaterial tier) {
        return switch (tier) {
            case IRON -> counts.gold() > 0 || counts.emerald() > 0 || counts.diamond() > 0;
            case GOLD -> counts.emerald() > 0 || counts.diamond() > 0;
            case EMERALD -> counts.diamond() > 0;
            case DIAMOND -> false;
        };
    }
}