package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Persistent server-side payload for a Rune block. */
public final class RuneBlockEntity extends BlockEntity {
    private int[] plan = SpellPresentation.emptyPlan();
    private int[] settings = SpellPresentation.emptySettings();
    private int resolvedRow;
    private Vec3 castYaw = new Vec3(0.0D, 0.0D, 1.0D);
    private Vec3 normal = new Vec3(0.0D, 1.0D, 0.0D);
    private Vec3 runeUp = new Vec3(0.0D, 0.0D, -1.0D);
    private UUID ownerUuid;
    private boolean armed;
    private boolean firing;

    public RuneBlockEntity(BlockPos pos, BlockState state) {
        super(SyntacticWizardry.RUNE_BLOCK_ENTITY.get(), pos, state);
    }

    public void initialize(SpellExecutionContext context, Vec3 resolvedNormal, Vec3 resolvedUp) {
        plan = context.plan().clone();
        settings = context.settings().clone();
        resolvedRow = context.row();
        castYaw = SpellExecutor.normalizeYaw(context.castYaw());
        normal = normalize(resolvedNormal, new Vec3(0.0D, 1.0D, 0.0D));
        runeUp = normalize(resolvedUp, ShapeResolution.surfaceUp(normal));
        ownerUuid = context.owner() == null ? null : context.owner().getUUID();
        armed = true;
        firing = false;
        setChanged();
    }

    public void trigger(Entity trigger) {
        if (!armed || firing || trigger == null || trigger.isRemoved() || trigger.isSpectator()) return;
        if (!(level instanceof ServerLevel server)) return;

        firing = true;
        armed = false;

        Entity owner = ownerUuid == null ? null : server.getEntity(ownerUuid);
        if (owner == null) owner = trigger;

        Vec3 origin = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
        ShapeResolution resolution = new ShapeResolution(
                origin,
                normal,
                runeUp,
                normal,
                List.of(worldPosition.immutable()),
                trigger,
                VectorPolicy.SURFACE_NORMAL);

        int[] triggerPlan = plan;
        int[] triggerSettings = settings;
        int triggerRow = resolvedRow;
        Vec3 triggerYaw = castYaw;

        server.removeBlock(worldPosition, false);
        SpellExecutor.continueFrom(server, owner, triggerPlan, triggerSettings, triggerRow, resolution, triggerYaw);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        plan = loadPlan(tag.getIntArray("Plan"));
        settings = loadSettings(tag.getIntArray("Settings"));
        resolvedRow = tag.getInt("ResolvedRow");
        castYaw = SpellExecutor.normalizeYaw(new Vec3(tag.getDouble("CastYawX"), 0.0D, tag.getDouble("CastYawZ")));
        normal = normalize(new Vec3(tag.getDouble("NormalX"), tag.getDouble("NormalY"), tag.getDouble("NormalZ")), new Vec3(0.0D, 1.0D, 0.0D));
        runeUp = normalize(new Vec3(tag.getDouble("RuneUpX"), tag.getDouble("RuneUpY"), tag.getDouble("RuneUpZ")), ShapeResolution.surfaceUp(normal));
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        armed = !tag.contains("Armed") || tag.getBoolean("Armed");
        firing = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putIntArray("Plan", plan);
        tag.putIntArray("Settings", settings);
        tag.putInt("ResolvedRow", resolvedRow);
        tag.putDouble("CastYawX", castYaw.x);
        tag.putDouble("CastYawZ", castYaw.z);
        tag.putDouble("NormalX", normal.x);
        tag.putDouble("NormalY", normal.y);
        tag.putDouble("NormalZ", normal.z);
        tag.putDouble("RuneUpX", runeUp.x);
        tag.putDouble("RuneUpY", runeUp.y);
        tag.putDouble("RuneUpZ", runeUp.z);
        tag.putBoolean("Armed", armed);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
    }

    private static Vec3 normalize(Vec3 value, Vec3 fallback) {
        return value != null && value.lengthSqr() > 1.0E-8D ? value.normalize() : fallback;
    }

    private static int[] loadPlan(int[] source) {
        int[] out = SpellPresentation.emptyPlan();
        if (source != null) System.arraycopy(source, 0, out, 0, Math.min(source.length, out.length));
        return out;
    }

    private static int[] loadSettings(int[] source) {
        int[] out = SpellPresentation.emptySettings();
        if (source != null) System.arraycopy(source, 0, out, 0, Math.min(source.length, out.length));
        return out;
    }
}
