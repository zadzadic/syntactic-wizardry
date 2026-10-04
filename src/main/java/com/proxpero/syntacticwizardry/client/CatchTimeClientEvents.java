package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.CatchTimeSetting;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CatchTimeClientEvents {
    private static final long DAY_LENGTH = 24000L;

    private CatchTimeClientEvents() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        CatchTimeSetting setting = CatchTimeClientState.currentSettingOverride();
        if (setting == null) return;
        long current = mc.level.getDayTime();
        long day = Math.floorDiv(current, DAY_LENGTH);
        mc.level.setDayTime(day * DAY_LENGTH + setting.dayTime());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CatchTimeClientState.clear();
    }
}