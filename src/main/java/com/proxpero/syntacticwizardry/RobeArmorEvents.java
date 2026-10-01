package com.proxpero.syntacticwizardry;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class RobeArmorEvents {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private RobeArmorEvents() {}

    @SubscribeEvent
    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (!(event.getStack().getItem() instanceof RobeArmorItem)) return;

        ItemEnchantments.Mutable enchantments = event.getEnchantments();
        for (Holder<Enchantment> enchantment : List.copyOf(enchantments.keySet())) {
            if (!event.isTargetting(enchantment)) continue;
            int level = enchantments.getLevel(enchantment);
            if (level > 0) enchantments.set(enchantment, level + 1);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack equipped = event.getEntity().getItemBySlot(slot);
            if (!(equipped.getItem() instanceof RobeArmorItem) || !RobeArmorItem.isDisabled(equipped)) continue;

            ItemStack dropped = equipped.copy();
            dropped.setDamageValue(Math.max(0, dropped.getMaxDamage() - 1));
            event.getEntity().setItemSlot(slot, ItemStack.EMPTY);
            event.getEntity().drop(dropped, false);
        }
    }
}
