package com.proxpero.syntacticwizardry;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class RobeArmorItem extends ArmorItem {
    private final float spellDiscountPerPiece;

    public RobeArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, float spellDiscountPerPiece) {
        super(material, type, properties);
        this.spellDiscountPerPiece = Math.max(0.0F, spellDiscountPerPiece);
    }

    public float spellDiscountPerPiece() {
        return spellDiscountPerPiece;
    }

    public static boolean isDisabled(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return !isDisabled(stack) && super.canEquip(stack, armorType, entity);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        if (amount <= 0 || !stack.isDamageableItem()) return 0;
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        if (remaining <= 1) return 0;
        return Math.min(amount, remaining - 1);
    }
}
