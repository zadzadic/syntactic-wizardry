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

public final class RunecasterItem extends Item {
    private static final float MANA_MULTIPLIER = 0.5F;

    public RunecasterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        for (int slot = 0; slot < RunecasterRuneStorage.RUNE_SLOTS; slot++) {
            ItemStack rune = RunecasterRuneStorage.getMountedRune(stack, slot);
            tooltip.add(Component.literal("Runestone " + (slot + 1) + ": " + (rune.isEmpty() ? "Empty" : rune.getHoverName().getString())));
        }
        tooltip.add(Component.literal("Mana cost: 50%"));
        tooltip.add(Component.literal("Melee damage: 2"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack runecaster = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) openRuneMenu(serverPlayer, runecaster);
            return InteractionResultHolder.sidedSuccess(runecaster, level.isClientSide());
        }

        ItemStack rawSpell = rawMountedSpell(runecaster);
        ItemStack spell = discountedMountedSpell(runecaster);
        if (spell.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("No active Runestone components are mounted."), true);
            return InteractionResultHolder.sidedSuccess(runecaster, level.isClientSide());
        }

        int[] plan = SpellPresentation.readPlan(spell);
        if (!startsWithShape(plan)) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("The mounted Rune must begin with a Shape."), true);
            return InteractionResultHolder.fail(runecaster);
        }

        int[] settings = SpellPresentation.readSettings(spell);
        boolean sustained = SpellComponents.requiresHeldUse(plan);

        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(runecaster);
        if (sustained) player.startUsingItem(hand);

        if (!level.isClientSide && level instanceof ServerLevel server) {
            float spellCost = RobeArmorSupport.discountedManaCost(player, SpellPresentation.readSpellCost(spell));
            float rawSpellCost = SpellPresentation.readSpellCost(rawSpell);

            if (!player.isCreative() && !ManaService.tryConsume(player, spellCost, rawSpellCost)) {
                if (sustained) player.stopUsingItem();
                return InteractionResultHolder.fail(runecaster);
            }

            player.getCooldowns().addCooldown(this, WrittenSpellItem.CAST_COOLDOWN_TICKS);
            ChannelRuntime.begin(player);
            StreamRuntime.begin(player);

            Vec3 origin = new Vec3(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            SpellExecutor.castRoot(server, player, plan, settings, origin, player.getLookAngle(), yawFacing(player.getYRot()));
        }

        return sustained
                ? InteractionResultHolder.consume(runecaster)
                : InteractionResultHolder.sidedSuccess(runecaster, level.isClientSide());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack spell = RunecasterSpellBuilder.build(RunecasterRuneStorage.getRune(stack));
        return !spell.isEmpty() && SpellComponents.requiresHeldUse(SpellPresentation.readPlan(spell)) ? 72000 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack runecaster, int remainingUseDuration) {
        if (level.isClientSide) return;

        ItemStack rawSpell = rawMountedSpell(runecaster);
        ItemStack spell = discountedMountedSpell(runecaster);

        if (spell.isEmpty()) {
            entity.stopUsingItem();
            ChannelRuntime.clear(entity);
            StreamRuntime.clear(entity);
            return;
        }

        int duration = getUseDuration(runecaster, entity);
        int elapsed = duration - remainingUseDuration;

        if (elapsed > 0
                && elapsed % WrittenSpellItem.SUSTAINED_MANA_INTERVAL_TICKS == 0
                && entity instanceof Player player
                && !player.isCreative()) {
            float sustainedCost = RobeArmorSupport.discountedManaCost(player, SpellPresentation.readSustainedCost(spell));
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

    private ItemStack rawMountedSpell(ItemStack runecaster) {
        ItemStack spell = RunecasterSpellBuilder.build(RunecasterRuneStorage.getRune(runecaster));
        if (!spell.isEmpty()) SpellPresentation.ensureManaCosts(spell);
        return spell;
    }

    private ItemStack discountedMountedSpell(ItemStack runecaster) {
        ItemStack spell = rawMountedSpell(runecaster);
        if (spell.isEmpty()) return ItemStack.EMPTY;
        SpellPresentation.applyDiscount(spell, MANA_MULTIPLIER);
        return spell;
    }

    private static boolean startsWithShape(int[] plan) {
        int firstRow = SpellPresentation.firstOccupiedRow(plan);
        if (firstRow < 0) return false;

        for (int col = 0; col < SpellPresentation.COLS; col++) {
            int type = SpellPresentation.typeAt(plan, firstRow * SpellPresentation.COLS + col);
            if (type == SpellPresentation.TYPE_EMPTY) continue;
            SpellComponentDefinition definition = SpellComponents.byType(type);
            return definition != null && definition.isShape();
        }
        return false;
    }

    private void openRuneMenu(ServerPlayer player, ItemStack runecaster) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new RunecasterMenu(id, inventory, runecaster),
                Component.translatable("menu.syntacticwizardry.runecaster")
        ));
    }

    private static Vec3 yawFacing(float yawDegrees) {
        double radians = Math.toRadians(yawDegrees);
        return new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
    }
}
