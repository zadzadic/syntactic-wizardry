package com.proxpero.syntacticwizardry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class RelativePreviewEvents {
    private static final int PREVIEW_INTERVAL_TICKS = 4;

    private RelativePreviewEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % PREVIEW_INTERVAL_TICKS != 0) return;
        if (previewHeld(player, player.getMainHandItem())) return;
        previewHeld(player, player.getOffhandItem());
    }

    private static boolean previewHeld(ServerPlayer player, ItemStack held) {
        ItemStack spell = spellFromHeld(held);
        if (spell.isEmpty()) return false;

        int[] plan = SpellPresentation.readPlan(spell);
        int[] settings = SpellPresentation.readSettings(spell);
        boolean found = false;

        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            if (SpellPresentation.typeAt(plan, cell) != SpellComponents.TYPE_RELATIVE) continue;
            found = true;
            int direction = SpellPresentation.settingAt(settings, cell, SpellPropertyKey.GRAVITY_MODE);
            int distance = SpellPresentation.distanceAt(settings, cell);
            ServerLevel level = player.serverLevel();
            Vec3 point = RelativeShape.previewLocation(level, player, direction, distance);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
        }

        return found;
    }

    private static ItemStack spellFromHeld(ItemStack held) {
        if (held == null || held.isEmpty()) return ItemStack.EMPTY;
        if (held.is(SyntacticWizardry.WRITTEN_SPELL.get())) return held;
        if (held.getItem() instanceof MagicFocusItem) return FocusSpellStorage.getActiveSpell(held);
        return ItemStack.EMPTY;
    }
}
