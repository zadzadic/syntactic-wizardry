package com.arcane.magic.compat;

import com.arcane.magic.builder.BuilderLinkSupport;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = "syntacticwizardry")
public final class BuilderLinkEventBridge {
    private BuilderLinkEventBridge() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BuilderLinkSupport.onRightClick(event);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        BuilderLinkSupport.tick(event);
    }
}
