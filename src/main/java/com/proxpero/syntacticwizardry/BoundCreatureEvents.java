package com.proxpero.syntacticwizardry;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class BoundCreatureEvents {
    private BoundCreatureEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();
        if (!(event.getTarget() instanceof Mob target)) return;

        BoundCreatureData data = BoundCreatureData.get(level.getServer());
        ItemStack held = player.getItemInHand(event.getHand());

        if (player.isShiftKeyDown() && held.is(SyntacticWizardry.WAND.get())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            if (data.isBoundTo(target.getUUID(), player.getUUID())) {
                player.displayClientMessage(
                        Component.literal("Bound creatures will not attack an ally."),
                        true);
                return;
            }

            int commanded = data.commandFollowers(player, target);
            player.displayClientMessage(
                    Component.literal(
                            commanded + (commanded == 1
                                    ? " following bound creature ordered to attack."
                                    : " following bound creatures ordered to attack.")),
                    true);
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BoundCreatureData.Entry entry = data.find(target.getUUID());
        if (entry == null || !entry.ownerId().equals(player.getUUID())) return;

        BoundCreatureData.Mode mode = data.cycleMode(target, player.getUUID());
        if (mode == null) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        player.displayClientMessage(
                Component.literal("Bound creature: " + mode.displayName() + "."),
                true);
    }

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel level)) return;

        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) return;

        BoundCreatureData data = BoundCreatureData.get(level.getServer());
        if (!data.targetAllowed(mob, target)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BoundCreatureData.get(level.getServer()).tickLevel(level);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob.level() instanceof ServerLevel level)) return;

        BoundCreatureData.get(level.getServer()).remove(mob.getUUID());
    }
}
