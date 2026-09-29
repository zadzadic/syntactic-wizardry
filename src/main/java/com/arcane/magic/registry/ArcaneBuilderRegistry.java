package com.arcane.magic.registry;

import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Compatibility bridge for the exact Arcane 0.4.267 Builder runtime. */
public final class ArcaneBuilderRegistry {
    private ArcaneBuilderRegistry() {}
    public static Block block() { return SyntacticWizardry.ARCANE_BUILDER.get(); }
    public static Item item() { return SyntacticWizardry.ARCANE_BUILDER_ITEM.get(); }
}
