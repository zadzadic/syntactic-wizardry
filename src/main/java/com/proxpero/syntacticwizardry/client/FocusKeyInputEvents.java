package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.FocusSlotCyclePayload;
import com.proxpero.syntacticwizardry.MagicFocusItem;
import com.proxpero.syntacticwizardry.RunecasterItem;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class FocusKeyInputEvents {
    private FocusKeyInputEvents() {}

    private static boolean holdingRunecaster(Minecraft mc) {
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().getItem() instanceof RunecasterItem
                || mc.player.getOffhandItem().getItem() instanceof RunecasterItem;
    }

    private static boolean holdingFocus(Minecraft mc) {
        if (mc.player == null) return false;
        return mc.player.getMainHandItem().getItem() instanceof MagicFocusItem
                || mc.player.getOffhandItem().getItem() instanceof MagicFocusItem;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (holdingRunecaster(mc)) {
            while (FocusKeyMappings.RUNE_LEFT.consumeClick()) {
                PacketDistributor.sendToServer(new FocusSlotCyclePayload(-100));
            }
            while (FocusKeyMappings.RUNE_RIGHT.consumeClick()) {
                PacketDistributor.sendToServer(new FocusSlotCyclePayload(100));
            }
        }

        if (!holdingFocus(mc)) return;
        while (FocusKeyMappings.PREVIOUS.consumeClick()) {
            PacketDistributor.sendToServer(new FocusSlotCyclePayload(-1));
        }
        while (FocusKeyMappings.NEXT.consumeClick()) {
            PacketDistributor.sendToServer(new FocusSlotCyclePayload(1));
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !holdingRunecaster(mc) || !Screen.hasControlDown()) return;
        double scroll = event.getScrollDeltaY();
        if (scroll == 0.0D) return;
        PacketDistributor.sendToServer(new FocusSlotCyclePayload(scroll > 0.0D ? -10 : 10));
        event.setCanceled(true);
    }
}
