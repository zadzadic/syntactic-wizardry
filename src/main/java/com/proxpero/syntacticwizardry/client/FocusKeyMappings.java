package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FocusKeyMappings {
    public static final KeyMapping PREVIOUS = new KeyMapping(
            "key.syntacticwizardry.previous_focus_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.misc");
    public static final KeyMapping NEXT = new KeyMapping(
            "key.syntacticwizardry.next_focus_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.misc");
    public static final KeyMapping RUNE_LEFT = new KeyMapping(
            "key.syntacticwizardry.rotate_runestone_left", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "key.categories.misc");
    public static final KeyMapping RUNE_RIGHT = new KeyMapping(
            "key.syntacticwizardry.rotate_runestone_right", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_PERIOD, "key.categories.misc");

    private FocusKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(PREVIOUS);
        event.register(NEXT);
        event.register(RUNE_LEFT);
        event.register(RUNE_RIGHT);
    }
}
