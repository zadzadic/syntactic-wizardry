package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class ManaGridSupport {
    private ManaGridSupport() {}
    public static int coreOutput(ServerLevel level, BlockPos core) {
        if (!PowerCoreSupport.isValid(level,core)) return 0;
        return PylonNetworkData.get(level).outputForCore(level,core);
    }
    public static int coreStorageCapacity(ServerLevel level, BlockPos core) { return PowerCoreSupport.storageCapacity(level,core); }
    public static double coreStored(ServerLevel level, BlockPos core) {
        if (!PowerCoreSupport.isValid(level,core)) return 0.0D;
        int capacity=coreStorageCapacity(level,core), output=coreOutput(level,core);
        return CoreManaData.get(level).current(level,core,capacity,output);
    }
    public static double consume(ServerLevel level, BlockPos core, double amount) {
        if (!PowerCoreSupport.isValid(level,core)) return 0.0D;
        int capacity=coreStorageCapacity(level,core), output=coreOutput(level,core);
        return CoreManaData.get(level).consume(level,core,capacity,output,amount);
    }
}
