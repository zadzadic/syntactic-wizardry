package com.proxpero.syntacticwizardry;

import net.minecraft.world.phys.Vec3;

/** Mana pricing used when a written spell is created at the Lectern. */
public final class SpellManaCost {
    public static final float DEFAULT_DISCOUNT = 1.0F;
    public static final float CHANNEL_SURCHARGE = 5.0F;
    public static final float STREAM_SURCHARGE = 5.0F;
    public static final float STREAM_SUSTAINED_MULTIPLIER = 0.5F;

    private static final Vec3 COST_ORIGIN = new Vec3(0.5D, 0.5D, 0.5D);
    private static final Vec3 COST_FORWARD = new Vec3(0.0D, 0.0D, 1.0D);
    private static final Vec3 COST_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private SpellManaCost() {}

    public record Costs(float initialCost, float initialSustainedCost, float discount, float spellCost, float sustainedCost) {}

    public static Costs calculate(int[] plan, int[] settings) {
        return calculate(plan, settings, DEFAULT_DISCOUNT);
    }

    public static Costs calculate(int[] plan, int[] settings, float discount) {
        float initialCost = totalInitialCost(plan, settings);
        float initialSustainedCost = totalInitialSustainedCost(plan, settings, initialCost);
        float appliedDiscount = Math.max(0.0F, discount);
        return new Costs(
                initialCost,
                initialSustainedCost,
                appliedDiscount,
                initialCost * appliedDiscount,
                initialSustainedCost * appliedDiscount
        );
    }

    public static float totalInitialCost(int[] plan, int[] settings) {
        float total = 0.0F;
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            total += componentCost(plan, settings, cell);
        }
        return total;
    }

    public static float totalInitialSustainedCost(int[] plan, int[] settings, float initialCost) {
        float total = 0.0F;
        boolean hasStream = false;
        for (int row = 0; row < SpellPresentation.ROWS; row++) {
            for (int col = 0; col < SpellPresentation.COLS; col++) {
                int cell = row * SpellPresentation.COLS + col;
                SpellComponentDefinition definition = SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
                if (definition == null) continue;
                if (SpellComponents.isEffect(definition) && SpellComponents.hasAttachedModifier(plan, row, col, SpellComponents.TYPE_CHANNEL)) {
                    total += componentCost(plan, settings, cell);
                }
                if (definition.isShape() && SpellComponents.hasAttachedModifier(plan, row, col, SpellComponents.TYPE_STREAM)) {
                    hasStream = true;
                }
            }
        }
        if (hasStream) total += initialCost * STREAM_SUSTAINED_MULTIPLIER;
        return total;
    }

    public static float componentCost(int[] plan, int[] settings, int cell) {
        int type = SpellPresentation.typeAt(plan, cell);
        if (type == SpellPresentation.TYPE_EMPTY) return 0.0F;
        int potence = SpellPresentation.potenceAt(settings, cell);
        return switch (type) {
            case SpellPresentation.TYPE_TOUCH -> 1.0F;
            case SpellPresentation.TYPE_MISSILE -> 2.0F;
            case SpellComponents.TYPE_CHAIN -> 2.0F;
            case SpellPresentation.TYPE_SPHERE -> SphereShape.voxels(COST_ORIGIN,
                    SpellPresentation.radiusAt(settings, cell),
                    SpellPresentation.sphereHeightAt(settings, cell),
                    SpellPresentation.sphereModeAt(settings, cell)).size();
            case SpellPresentation.TYPE_BOX -> BoxShape.voxels(COST_ORIGIN, COST_FORWARD, COST_UP,
                    SpellPresentation.boxWidthAt(settings, cell),
                    SpellPresentation.boxHeightAt(settings, cell),
                    SpellPresentation.boxDepthAt(settings, cell)).size();
            case SpellPresentation.TYPE_CONE -> ConeShape.voxels(COST_ORIGIN, COST_FORWARD,
                    SpellPresentation.boxWidthAt(settings, cell),
                    SpellPresentation.boxHeightAt(settings, cell),
                    SpellPresentation.boxDepthAt(settings, cell)).size();
            case SpellPresentation.TYPE_FLOATING -> 3.0F;
            case SpellPresentation.TYPE_TARGET -> 3.0F;
            case SpellComponents.TYPE_RUNE -> 0.0F; // Rune pricing has not been defined yet.
            case SpellComponents.TYPE_RELATIVE -> 3.0F;
            case SpellPresentation.TYPE_DAMAGE -> 3.0F * potence;
            case SpellPresentation.TYPE_DIG -> 1.0F * potence;
            case SpellComponents.TYPE_RESTORE -> 1.0F * potence;
            case SpellComponents.TYPE_MOVE -> 0.5F * potence;
            case SpellComponents.TYPE_SIPHON -> 3.0F * potence;
            case SpellComponents.TYPE_GRAVITY -> 1.0F * potence;
            case SpellComponents.TYPE_ALTER -> 0.0F; // Alter pricing has not been defined yet.
            case SpellComponents.TYPE_PROTECTION -> 5.0F * potence;
            case SpellComponents.TYPE_TEMPORARY_BLOCK -> temporaryBlockCost(plan, settings, cell);
            case SpellComponents.TYPE_DURATION -> durationCost(plan, settings, cell);
            case SpellComponents.TYPE_BLOCK_INTERACTION -> 0.0F;
            case SpellComponents.TYPE_SELF -> 0.0F;
            case SpellComponents.TYPE_CHANNEL -> CHANNEL_SURCHARGE;
            case SpellComponents.TYPE_STREAM -> STREAM_SURCHARGE;
            default -> 0.0F;
        };
    }
    private static float durationCost(int[] plan, int[] settings, int cell) {
        int row = cell / SpellPresentation.COLS;
        int col = cell % SpellPresentation.COLS;
        for (int ownerCol = col - 1; ownerCol >= 0; ownerCol--) {
            int ownerType = SpellPresentation.typeAt(plan, row * SpellPresentation.COLS + ownerCol);
            if (ownerType == SpellPresentation.TYPE_EMPTY) continue;
            SpellComponentDefinition owner = SpellComponents.byType(ownerType);
            if (owner == null || SpellComponents.isModifier(owner)) continue;
            if (ownerType == SpellComponents.TYPE_TEMPORARY_BLOCK) return 0.0F;
            break;
        }
        return 1.0F * SpellPresentation.durationSecondsAt(settings, cell);
    }

    private static float temporaryBlockCost(int[] plan, int[] settings, int cell) {
        int row = cell / SpellPresentation.COLS;
        int col = cell % SpellPresentation.COLS;
        if (SpellComponents.hasAttachedModifier(plan, row, col, SpellComponents.TYPE_DURATION)) return 0.0F;
        return incomingVoxelCount(plan, settings, row);
    }

    /**
     * Estimates the total voxel occupancy delivered to a row by the spell grammar.
     * Temporary Block costs one Mana per incoming voxel unless Duration is attached.
     */
    private static float incomingVoxelCount(int[] plan, int[] settings, int targetRow) {
        long incomingBranches = 1L;
        long incomingVoxels = 0L;
        for (int row = 0; row < targetRow; row++) {
            long shapeVoxels = 0L;
            int continuationShapes = 0;
            for (int col = 0; col < SpellPresentation.COLS; col++) {
                int shapeCell = row * SpellPresentation.COLS + col;
                int type = SpellPresentation.typeAt(plan, shapeCell);
                SpellComponentDefinition definition = SpellComponents.byType(type);
                if (definition == null || !definition.isShape()) continue;
                if (type == SpellComponents.TYPE_RUNE) continue;
                continuationShapes++;
                shapeVoxels += estimatedShapeVoxelCount(type, settings, shapeCell);
            }
            if (continuationShapes == 0) continue;
            incomingVoxels = saturatedMultiply(incomingBranches, shapeVoxels);
            incomingBranches = saturatedMultiply(incomingBranches, continuationShapes);
        }
        return Math.min((float) incomingVoxels, Float.MAX_VALUE);
    }

    private static long estimatedShapeVoxelCount(int type, int[] settings, int cell) {
        return switch (type) {
            case SpellPresentation.TYPE_SPHERE -> SphereShape.voxels(COST_ORIGIN,
                    SpellPresentation.radiusAt(settings, cell),
                    SpellPresentation.sphereHeightAt(settings, cell),
                    SpellPresentation.sphereModeAt(settings, cell)).size();
            case SpellPresentation.TYPE_BOX -> BoxShape.voxels(COST_ORIGIN, COST_FORWARD, COST_UP,
                    SpellPresentation.boxWidthAt(settings, cell),
                    SpellPresentation.boxHeightAt(settings, cell),
                    SpellPresentation.boxDepthAt(settings, cell)).size();
            case SpellPresentation.TYPE_CONE -> ConeShape.voxels(COST_ORIGIN, COST_FORWARD,
                    SpellPresentation.boxWidthAt(settings, cell),
                    SpellPresentation.boxHeightAt(settings, cell),
                    SpellPresentation.boxDepthAt(settings, cell)).size();
            case SpellComponents.TYPE_SELF -> 0L;
            default -> 1L;
        };
    }

    private static long saturatedMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) return 0L;
        if (left > Long.MAX_VALUE / right) return Long.MAX_VALUE;
        return left * right;
    }

}
