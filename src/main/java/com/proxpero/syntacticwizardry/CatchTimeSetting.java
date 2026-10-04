package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;

/**
 * Catch Time settings. Gold offsets are fixed positions relative to the
 * Mature Crystal activation center. The Lapis Block is directly below it.
 */
public enum CatchTimeSetting {
    DAWN("Dawn", 0L, new BlockPos(1, -1, 0)),
    NOON("Noon", 6000L, new BlockPos(0, -1, 1)),
    TWILIGHT("Twilight", 12000L, new BlockPos(-1, -1, 0)),
    MIDNIGHT("Midnight", 18000L, new BlockPos(0, -1, -1));

    private final String displayName;
    private final long dayTime;
    private final BlockPos goldOffset;

    CatchTimeSetting(String displayName, long dayTime, BlockPos goldOffset) {
        this.displayName = displayName;
        this.dayTime = dayTime;
        this.goldOffset = goldOffset;
    }

    public String displayName() {
        return displayName;
    }

    public long dayTime() {
        return dayTime;
    }

    /** Fixed Gold Block position relative to the Mature Crystal. */
    public BlockPos goldOffset() {
        return goldOffset;
    }

    public CatchTimeSetting previous() {
        CatchTimeSetting[] values = values();
        return values[Math.floorMod(ordinal() - 1, values.length)];
    }

    public CatchTimeSetting next() {
        CatchTimeSetting[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}