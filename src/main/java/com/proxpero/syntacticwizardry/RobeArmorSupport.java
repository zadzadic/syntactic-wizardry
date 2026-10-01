package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class RobeArmorSupport {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private RobeArmorSupport() {}

    public static float spellDiscount(LivingEntity wearer) {
        if (wearer == null) return 0.0F;
        float discount = 0.0F;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = wearer.getItemBySlot(slot);
            if (stack.getItem() instanceof RobeArmorItem robe && !RobeArmorItem.isDisabled(stack)) {
                discount += robe.spellDiscountPerPiece();
            }
        }
        return Math.min(1.0F, discount);
    }

    public static float manaMultiplier(LivingEntity wearer) {
        return Math.max(0.0F, 1.0F - spellDiscount(wearer));
    }

    public static float discountedManaCost(LivingEntity wearer, float baseCost) {
        return Math.max(0.0F, baseCost) * manaMultiplier(wearer);
    }
}
