package com.proxpero.syntacticwizardry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Registers the exact 0.4.267 Builder seat lifecycle under the current mod id. */
@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LegacyBuilderSeatEvents {
    private static final String RUNTIME = "com.arcane.magic.mana.ArcaneBuilderSeatEvents";
    private LegacyBuilderSeatEvents() {}
    @SubscribeEvent public static void onTick(PlayerTickEvent.Post event) { LegacyBuilderBridge.invoke(RUNTIME, "onTick", event); }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { LegacyBuilderBridge.invoke(RUNTIME, "onLogout", event); }
}
