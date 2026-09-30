package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.MagicLightRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = "syntacticwizardry", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MagicLightClientEvents {
    private MagicLightClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MagicLightRegistry.blockEntityType(), MagicLightRenderer::new);
    }
}
