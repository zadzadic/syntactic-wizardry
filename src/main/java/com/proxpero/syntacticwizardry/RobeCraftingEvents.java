package com.proxpero.syntacticwizardry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class RobeCraftingEvents {
    private RobeCraftingEvents() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!isHeavyRobe(result)) return;

        Container grid = event.getInventory();
        DyeColor first = null;
        boolean mixed = false;

        for (int i = 0; i < grid.getContainerSize(); i++) {
            ItemStack ingredient = grid.getItem(i);
            if (ingredient.isEmpty() || !ingredient.is(ItemTags.WOOL)) continue;

            DyeColor color = DyeColor.getColor(ingredient);
            if (color == null) continue;
            if (first == null) first = color;
            else if (first != color) mixed = true;
        }

        DyeColor outputColor = mixed || first == null ? DyeColor.BROWN : first;
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(outputColor.getTextureDiffuseColor(), false));
    }

    private static boolean isHeavyRobe(ItemStack stack) {
        return stack.is(SyntacticWizardry.HEAVY_HOOD.get())
                || stack.is(SyntacticWizardry.HEAVY_UPPER_ROBE.get())
                || stack.is(SyntacticWizardry.HEAVY_LOWER_ROBE.get());
    }
}
