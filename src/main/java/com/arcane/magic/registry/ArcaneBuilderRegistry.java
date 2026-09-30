package com.arcane.magic.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Bridge from the original Builder bytecode to the Syntactic Wizardry registry. */
public final class ArcaneBuilderRegistry {
    private ArcaneBuilderRegistry() {}
    public static Block block() { return com.proxpero.syntacticwizardry.ArcaneBuilderRegistry.block(); }
    public static Item item() { return com.proxpero.syntacticwizardry.ArcaneBuilderRegistry.item(); }
}
