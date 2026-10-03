package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ConduitRegistry;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ConduitPlannerClientEvents {
    private ConduitPlannerClientEvents() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) return;
        if (ConduitRegistry.block() == null) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(ConduitRegistry.block())) return;
        if (!event.getItemStack().isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        ConduitPlannerState.begin(event.getPos());
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (ConduitPlannerState.scrollRitualList(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }
}
