package com.proxpero.syntacticwizardry;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MagicFocusItem extends Item {
    private final int spellSlots;
    private final float manaMultiplier;

    public MagicFocusItem(Properties properties, int spellSlots, float manaMultiplier) {
        super(properties);
        this.spellSlots = Math.max(1, spellSlots);
        this.manaMultiplier = Math.max(0.0F, manaMultiplier);
    }

    public int spellSlots() { return spellSlots; }
    public float manaMultiplier() { return manaMultiplier; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Spell slots: " + spellSlots));
        tooltip.add(Component.literal("Mana cost: " + Math.round(manaMultiplier * 100.0F) + "%"));
        int active = FocusSpellStorage.activeSlot(stack);
        ItemStack spell = FocusSpellStorage.getSlot(stack, active);
        tooltip.add(Component.literal("Active: " + (spell.isEmpty() ? "Empty" : spell.getHoverName().getString())));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack focus = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) openFocusMenu(serverPlayer, focus);
            return InteractionResultHolder.sidedSuccess(focus, level.isClientSide());
        }

        ItemStack rawSpell = rawActiveSpell(focus);
        ItemStack spell = discountedActiveSpell(focus);
        if (spell.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("Focus slot " + (FocusSpellStorage.activeSlot(focus) + 1) + " is empty."), true);
            return InteractionResultHolder.sidedSuccess(focus, level.isClientSide());
        }

        SpellPresentation.ensureManaCosts(spell);
        int[] plan = SpellPresentation.readPlan(spell);
        int[] settings = SpellPresentation.readSettings(spell);
        boolean sustained = SpellComponents.requiresHeldUse(plan);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(focus);
        if (sustained) player.startUsingItem(hand);

        if (!level.isClientSide && level instanceof ServerLevel server) {
            float spellCost = SpellPresentation.readSpellCost(spell);
            float rawSpellCost = SpellPresentation.readSpellCost(rawSpell);
            if (!player.isCreative() && !ManaService.tryConsume(player, spellCost, rawSpellCost)) {
                if (sustained) player.stopUsingItem();
                return InteractionResultHolder.fail(focus);
            }
            player.getCooldowns().addCooldown(this, WrittenSpellItem.CAST_COOLDOWN_TICKS);
            ChannelRuntime.begin(player);
            StreamRuntime.begin(player);
            Vec3 origin = new Vec3(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            SpellExecutor.castRoot(server, player, plan, settings, origin, player.getLookAngle(), yawFacing(player.getYRot()));
        }
        return sustained ? InteractionResultHolder.consume(focus) : InteractionResultHolder.sidedSuccess(focus, level.isClientSide());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack spell = FocusSpellStorage.getActiveSpell(stack);
        return !spell.isEmpty() && SpellComponents.requiresHeldUse(SpellPresentation.readPlan(spell)) ? 72000 : 0;
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.NONE; }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack focus, int remainingUseDuration) {
        if (level.isClientSide) return;
        ItemStack rawSpell = rawActiveSpell(focus);
        ItemStack spell = discountedActiveSpell(focus);
        if (spell.isEmpty()) {
            entity.stopUsingItem();
            ChannelRuntime.clear(entity);
            StreamRuntime.clear(entity);
            return;
        }
        int duration = getUseDuration(focus, entity);
        int elapsed = duration - remainingUseDuration;
        if (elapsed > 0 && elapsed % WrittenSpellItem.SUSTAINED_MANA_INTERVAL_TICKS == 0 && entity instanceof Player player && !player.isCreative()) {
            float sustainedCost = SpellPresentation.readSustainedCost(spell);
            float rawSustainedCost = SpellPresentation.readSustainedCost(rawSpell);
            if (sustainedCost > 0.0F && !ManaService.tryConsume(player, sustainedCost, rawSustainedCost)) {
                entity.stopUsingItem();
                ChannelRuntime.clear(entity);
                StreamRuntime.clear(entity);
                return;
            }
        }
        ChannelRuntime.tick(entity);
        StreamRuntime.tick(entity);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide) {
            ChannelRuntime.clear(entity);
            StreamRuntime.clear(entity);
        }
    }

    protected ItemStack rawActiveSpell(ItemStack focus) {
        ItemStack spell = FocusSpellStorage.getActiveSpell(focus);
        if (spell.isEmpty()) return ItemStack.EMPTY;
        spell = spell.copy();
        SpellPresentation.ensureManaCosts(spell);
        return spell;
    }

    protected ItemStack discountedActiveSpell(ItemStack focus) {
        ItemStack spell = FocusSpellStorage.getActiveSpell(focus);
        if (spell.isEmpty()) return ItemStack.EMPTY;
        spell = spell.copy();
        SpellPresentation.ensureManaCosts(spell);
        SpellPresentation.applyDiscount(spell, manaMultiplier);
        return spell;
    }

    private void openFocusMenu(ServerPlayer player, ItemStack focus) {
        int slots = spellSlots;
        int active = FocusSpellStorage.activeSlot(focus);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new FocusSpellMenu(id, inventory, focus),
                Component.translatable("menu.syntacticwizardry.focus")
        ), buffer -> {
            buffer.writeVarInt(slots);
            buffer.writeVarInt(active);
        });
    }

    private static Vec3 yawFacing(float yawDegrees) {
        double radians = Math.toRadians(yawDegrees);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
    }
}
