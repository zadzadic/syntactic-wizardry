package com.arcane.magic.registry;

import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.world.item.Item;

/** Compatibility bridge used by the legacy Builder's Wand interaction code. */
public final class FocusItemRegistry {
    private FocusItemRegistry() {}
    public static Item wand() { return SyntacticWizardry.WAND.get(); }
}
