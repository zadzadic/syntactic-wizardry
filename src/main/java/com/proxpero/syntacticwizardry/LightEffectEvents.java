package com.proxpero.syntacticwizardry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LightEffectEvents {
    private LightEffectEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LightEffectRuntime.tick(event.getServer());
    }
}
