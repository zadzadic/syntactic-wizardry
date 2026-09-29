package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ManaService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = "syntacticwizardry", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ManaHudLayer {
    private static final String MOD_ID = "syntacticwizardry";

    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "mana_hud");
    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/mana_vial_frame.png");
    private static final ResourceLocation[] FILL_TEXTURES = new ResourceLocation[] {
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/mana_fill_0.png"),
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/mana_fill_1.png"),
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/mana_fill_2.png")
    };
    private static final ResourceLocation PLATE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/mana_nameplate.png");

    private static final int FRAME_TEX_W = 724;
    private static final int FRAME_TEX_H = 2172;
    private static final int FILL_TEX_W = 724;
    private static final int FILL_TEX_H = 2172;
    private static final int PLATE_TEX_W = 2172;
    private static final int PLATE_TEX_H = 724;

    private static final int FRAME_W = 24;
    private static final int FRAME_H = 72;
    private static final int PLATE_W = 56;
    private static final int PLATE_H = 18;
    private static final int MARGIN = 6;
    private static final LayeredDraw.Layer LAYER = ManaHudLayer::render;

    private ManaHudLayer() {}

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, LAYER);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) return;
        LocalPlayer player = minecraft.player;
        Font font = minecraft.font;
        if (player == null || font == null) return;

        float mana = ManaService.getMana(player);
        float maxMana = ManaService.getMaxMana(player);
        float fraction = Math.max(0.0F, Math.min(1.0F, maxMana <= 0.0F ? 0.0F : mana / maxMana));

        int guiHeight = guiGraphics.guiHeight();
        int plateX = 4;
        int plateY = guiHeight - PLATE_H - MARGIN;
        int frameX = plateX + (PLATE_W - FRAME_W) / 2;
        int frameY = plateY - FRAME_H - 4;
        int textCenterX = plateX + (PLATE_W / 2);
        int textY = plateY + ((PLATE_H - 8) / 2);

        drawPartialFill(guiGraphics, animatedFill(player.tickCount), frameX, frameY, FRAME_W, FRAME_H, fraction);
        guiGraphics.blit(FRAME_TEXTURE, frameX, frameY, FRAME_W, FRAME_H, 0.0F, 0.0F, FRAME_TEX_W, FRAME_TEX_H, FRAME_TEX_W, FRAME_TEX_H);
        guiGraphics.blit(PLATE_TEXTURE, plateX, plateY, PLATE_W, PLATE_H, 0.0F, 0.0F, PLATE_TEX_W, PLATE_TEX_H, PLATE_TEX_W, PLATE_TEX_H);

        String valueText = formatMana(mana);
        int textX = textCenterX - (font.width(valueText) / 2);
        guiGraphics.drawString(font, valueText, textX, textY, 0xEAF6FF, true);
    }

    private static String formatMana(float mana) {
        int rounded = Math.round(mana);
        if (Math.abs(mana - rounded) < 0.001F) return Integer.toString(rounded);
        return String.format(java.util.Locale.ROOT, "%.1f", mana);
    }

    private static ResourceLocation animatedFill(int tickCount) {
        int index = Math.floorMod(tickCount / 8, FILL_TEXTURES.length);
        return FILL_TEXTURES[index];
    }

    private static void drawPartialFill(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height, float fraction) {
        int visibleHeight = Math.max(0, Math.min(height, Math.round(height * fraction)));
        if (visibleHeight <= 0) return;
        int sourceHeight = Math.max(1, Math.round(FILL_TEX_H * (visibleHeight / (float) height)));
        float vOffset = FILL_TEX_H - sourceHeight;
        int destY = y + (height - visibleHeight);
        guiGraphics.blit(texture, x, destY, width, visibleHeight, 0.0F, vOffset, FILL_TEX_W, sourceHeight, FILL_TEX_W, FILL_TEX_H);
    }
}
