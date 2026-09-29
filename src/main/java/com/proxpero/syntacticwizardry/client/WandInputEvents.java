package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.SyntacticWizardry;
import com.proxpero.syntacticwizardry.WandClearBindingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class WandInputEvents {
    private WandInputEvents() {}

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || !Screen.hasControlDown()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        ItemStack stack = minecraft.player.getItemInHand(event.getHand());
        if (!stack.is(SyntacticWizardry.WAND.get())) return;
        PacketDistributor.sendToServer(new WandClearBindingPayload(event.getHand() == InteractionHand.MAIN_HAND));
        event.setSwingHand(false);
        event.setCanceled(true);
    }
}
