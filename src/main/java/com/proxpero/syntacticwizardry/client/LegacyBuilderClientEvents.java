package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.LegacyBuilderBridge;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Registers the exact Arcane 0.4.267 Builder client/editor handlers under this mod id. */
@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class LegacyBuilderClientEvents {
    private static final String CLIENT = "com.arcane.magic.client.ArcaneBuilderClientEvents";
    private static final String CURSOR = "com.arcane.magic.client.ArcaneBuilderCursorEvents";
    private static final String EDIT = "com.arcane.magic.client.ArcaneBuilderEditEvents";
    private static final String CONTROLLER = "com.arcane.magic.client.ArcaneBuilderControllerEvents";
    private static final String VISUAL = "com.arcane.magic.client.ArcaneBuilderVisualEvents";
    private static final String HOLD = "com.arcane.magic.builder.BuilderHoldSupportSubscriber";
    private static final String SLICE = "com.arcane.magic.builder.BuilderSliceEvents";

    private LegacyBuilderClientEvents() {}

    @SubscribeEvent public static void onRightClick(PlayerInteractEvent.RightClickBlock event) { LegacyBuilderBridge.invoke(CLIENT, "onRightClick", event); }

    @SubscribeEvent public static void onClientTick(ClientTickEvent.Post event) {
        LegacyBuilderBridge.invoke(CLIENT, "onClientTick", event);
        LegacyBuilderBridge.invoke(CURSOR, "onClientTick", event);
        LegacyBuilderBridge.invoke(EDIT, "onClientTick", event);
        LegacyBuilderBridge.invoke(CONTROLLER, "onClientTick", event);
        LegacyBuilderBridge.invoke(HOLD, "onClientTick", event);
        LegacyBuilderBridge.invoke(SLICE, "onClientTick", event);
    }

    @SubscribeEvent public static void onScroll(InputEvent.MouseScrollingEvent event) { LegacyBuilderBridge.invoke(CLIENT, "onScroll", event); }
    @SubscribeEvent public static void onKey(InputEvent.Key event) { LegacyBuilderBridge.invoke(CLIENT, "onBuilderStorageKey", event); LegacyBuilderBridge.invoke(SLICE, "onKey", event); }
    @SubscribeEvent public static void onMouse(InputEvent.MouseButton.Pre event) { LegacyBuilderBridge.invoke(EDIT, "consumeMouse", event); }
    @SubscribeEvent public static void onGuiLayer(RenderGuiLayerEvent.Pre event) { LegacyBuilderBridge.invoke(EDIT, "hideHotbar", event); LegacyBuilderBridge.invoke(VISUAL, "hideCrosshair", event); }
    @SubscribeEvent public static void onGui(RenderGuiEvent.Post event) { LegacyBuilderBridge.invoke(EDIT, "onRenderGui", event); LegacyBuilderBridge.invoke(CONTROLLER, "onRenderGui", event); }
    @SubscribeEvent public static void onLevel(RenderLevelStageEvent event) { LegacyBuilderBridge.invoke(EDIT, "onRenderLevel", event); LegacyBuilderBridge.invoke(VISUAL, "renderGrid", event); }
    @SubscribeEvent public static void onHand(RenderHandEvent event) { LegacyBuilderBridge.invoke(VISUAL, "hideHand", event); }
}
