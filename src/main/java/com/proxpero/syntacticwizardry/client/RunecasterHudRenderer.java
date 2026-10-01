package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RunecasterItem;
import com.proxpero.syntacticwizardry.RunecasterRuneStorage;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class RunecasterHudRenderer {
    private RunecasterHudRenderer() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || minecraft.options.hideGui) return;

        ItemStack runecaster = heldRunecaster(minecraft);
        if (runecaster.isEmpty()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int x = 14;
        int y = Math.max(18, graphics.guiHeight() / 2 - 34);

        for (int slot = 0; slot < RunecasterRuneStorage.RUNE_SLOTS; slot++) {
            int type = RunecasterRuneStorage.activeType(runecaster, slot);
            SpellComponentDefinition definition = SpellComponents.byType(type);
            if (definition == null) continue;

            ItemStack icon = definition.createEditorIcon();
            if (icon.isEmpty()) continue;
            graphics.renderItem(icon, x, y + slot * 20);
        }
    }

    private static ItemStack heldRunecaster(Minecraft minecraft) {
        ItemStack main = minecraft.player.getMainHandItem();
        if (main.getItem() instanceof RunecasterItem) return main;
        ItemStack off = minecraft.player.getOffhandItem();
        if (off.getItem() instanceof RunecasterItem) return off;
        return ItemStack.EMPTY;
    }
}
