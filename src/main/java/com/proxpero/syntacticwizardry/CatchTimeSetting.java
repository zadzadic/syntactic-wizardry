package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;

public enum CatchTimeSetting {
    DAWN("Dawn", 0L, new BlockPos(1, 0, 0)),
    NOON("Noon", 6000L, new BlockPos(0, 0, 1)),
    TWILIGHT("Twilight", 12000L, new BlockPos(-1, 0, 0)),
    MIDNIGHT("Midnight", 18000L, new BlockPos(0, 0, -1));

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