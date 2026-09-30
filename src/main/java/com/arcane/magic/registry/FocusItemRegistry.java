package com.arcane.magic.registry;

import net.minecraft.world.item.Item;

/** Bridge from the original Builder bytecode to the current Wand item. */
public final class FocusItemRegistry {
    private FocusItemRegistry() {}
    public static Item wand() { return com.proxpero.syntacticwizardry.SyntacticWizardry.WAND.get(); }
}
