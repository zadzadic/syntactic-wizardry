package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.EclipseRitualStructure;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.RitualStructureRules;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class PreparedRitualPreview {
    public record GhostBlock(
            BlockPos pos,
            RitualStructureRules.Role role,
            RitualStructureRules.FocusMaterial focusMaterial,
            String label) {}

    private static RitualDefinition ritual;
    private static BlockPos center;
    private static int requestedPotence;
    private static RitualStructureRules.FocusPlan focusPlan;
    private static List<GhostBlock> ghosts = List.of();

    private PreparedRitualPreview() {}

    public static void prepare(RitualDefinition definition, BlockPos ritualCenter) {
        prepare(definition, ritualCenter, 1);
    }

    public static void prepare(RitualDefinition definition, BlockPos ritualCenter, int potence) {
        if (definition == null || ritualCenter == null) return;
        ritual = definition;
        center = ritualCenter.immutable();
        requestedPotence = Math.max(1, potence);
        focusPlan = RitualStructureRules.focusPlan(requestedPotence);
        ghosts = previewGhosts(definition, center, requestedPotence);
    }

    public static List<GhostBlock> previewGhosts(
            RitualDefinition definition,
            BlockPos ritualCenter,
            int potence) {
        if (definition == null || ritualCenter == null) return List.of();

        BlockPos previewCenter = ritualCenter.immutable();
        RitualStructureRules.FocusPlan previewFocus = RitualStructureRules.focusPlan(Math.max(1, potence));
        List<GhostBlock> built = new ArrayList<>();

        built.add(new GhostBlock(
                previewCenter,
                RitualStructureRules.Role.CENTER,
                null,
                "Mature Crystal"));

        if (definition == RitualDefinition.ECLIPSE) {
            built.add(new GhostBlock(
                    previewCenter.below(),
                    RitualStructureRules.Role.STRUCTURAL,
                    null,
                    "Obsidian"));
            for (BlockPos offset : EclipseRitualStructure.runeOffsets()) {
                built.add(new GhostBlock(
                        previewCenter.offset(offset),
                        RitualStructureRules.Role.RUNE,
                        null,
                        "Chalk Rune"));
            }
        }

        for (RitualStructureRules.FocusPlacement placement : previewFocus.placements()) {
            built.add(new GhostBlock(
                    placement.position(previewCenter),
                    RitualStructureRules.Role.FOCUS,
                    placement.material(),
                    placement.material().displayName() + " Focus"));
        }

        return Collections.unmodifiableList(built);
    }

    public static boolean prepared() {
        return ritual != null && center != null;
    }

    public static RitualDefinition ritual() {
        return ritual;
    }

    public static BlockPos center() {
        return center;
    }

    public static int requestedPotence() {
        return requestedPotence;
    }

    public static RitualStructureRules.FocusPlan focusPlan() {
        return focusPlan;
    }

    public static List<GhostBlock> ghosts() {
        return ghosts;
    }

    public static void clear() {
        ritual = null;
        center = null;
        requestedPotence = 0;
        focusPlan = null;
        ghosts = List.of();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
        ActiveRitualClientRegistry.clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (!prepared()) return;
        renderGhosts(event, ghosts, 0.95F);
    }

    public static void renderGhosts(RenderLevelStageEvent event, List<GhostBlock> blocks, float alpha) {
        if (event == null || blocks == null || blocks.isEmpty()
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lines = RenderType.lines();
        VertexConsumer consumer = buffers.getBuffer(lines);

        for (GhostBlock ghost : blocks) {
            BlockPos pos = ghost.pos();
            double pulse = 0.03D + 0.015D * Math.sin(
                    (mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.14D);

            float[] color = colorFor(ghost);
            LevelRenderer.renderLineBox(
                    pose, consumer,
                    pos.getX() - camera.x - pulse,
                    pos.getY() - camera.y - pulse,
                    pos.getZ() - camera.z - pulse,
                    pos.getX() - camera.x + 1.0D + pulse,
                    pos.getY() - camera.y + 1.0D + pulse,
                    pos.getZ() - camera.z + 1.0D + pulse,
                    color[0], color[1], color[2], Math.max(0.10F, Math.min(1.0F, alpha)));
        }

        buffers.endBatch(lines);
    }

    private static float[] colorFor(GhostBlock ghost) {
        if (ghost.role() == RitualStructureRules.Role.CENTER) {
            return new float[]{0.72F, 0.42F, 1.0F};
        }

        if (ghost.role() == RitualStructureRules.Role.RUNE) {
            return new float[]{0.72F, 0.42F, 1.0F};
        }
        if (ghost.role() == RitualStructureRules.Role.STRUCTURAL) {
            return new float[]{0.34F, 0.20F, 0.42F};
        }
        if (ghost.role() != RitualStructureRules.Role.FOCUS || ghost.focusMaterial() == null) {
            return new float[]{0.70F, 0.70F, 0.70F};
        }

        return switch (ghost.focusMaterial()) {
            case IRON -> new float[]{0.78F, 0.78F, 0.82F};
            case GOLD -> new float[]{1.00F, 0.78F, 0.18F};
            case EMERALD -> new float[]{0.18F, 0.90F, 0.38F};
            case DIAMOND -> new float[]{0.28F, 0.92F, 1.00F};
        };
    }
}
