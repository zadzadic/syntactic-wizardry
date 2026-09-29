package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.FocusSlotCyclePayload;
import com.proxpero.syntacticwizardry.MagicFocusItem;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class FocusKeyInputEvents {
    private FocusKeyInputEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        if (!(minecraft.player.getMainHandItem().getItem() instanceof MagicFocusItem)
                && !(minecraft.player.getOffhandItem().getItem() instanceof MagicFocusItem)) return;
        while (FocusKeyMappings.PREVIOUS.consumeClick()) PacketDistributor.sendToServer(new FocusSlotCyclePayload(-1));
        while (FocusKeyMappings.NEXT.consumeClick()) PacketDistributor.sendToServer(new FocusSlotCyclePayload(1));
    }
}
