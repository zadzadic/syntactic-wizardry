package com.proxpero.syntacticwizardry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.proxpero.syntacticwizardry.RitualDefinition;
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
    public record GhostBlock(BlockPos pos, String role) {}

    private static RitualDefinition ritual;
    private static BlockPos center;
    private static List<GhostBlock> ghosts = List.of();

    private PreparedRitualPreview() {}

    public static void prepare(RitualDefinition definition, BlockPos ritualCenter) {
        ritual = definition;
        center = ritualCenter == null ? BlockPos.ZERO : ritualCenter.immutable();

        List<GhostBlock> built = new ArrayList<>();
        built.add(new GhostBlock(center, "Mature Crystal"));
        ghosts = Collections.unmodifiableList(built);
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

    public static List<GhostBlock> ghosts() {
        return ghosts;
    }

    public static void clear() {
        ritual = null;
        center = null;
        ghosts = List.of();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
        ActiveRitualClientRegistry.clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (!prepared() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lines = RenderType.lines();
        VertexConsumer consumer = buffers.getBuffer(lines);

        for (GhostBlock ghost : ghosts) {
            BlockPos pos = ghost.pos();
            double pulse = 0.03D + 0.015D * Math.sin((mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.14D);
            LevelRenderer.renderLineBox(
                    pose, consumer,
                    pos.getX() - camera.x - pulse,
                    pos.getY() - camera.y - pulse,
                    pos.getZ() - camera.z - pulse,
                    pos.getX() - camera.x + 1.0D + pulse,
                    pos.getY() - camera.y + 1.0D + pulse,
                    pos.getZ() - camera.z + 1.0D + pulse,
                    0.72F, 0.42F, 1.0F, 0.95F);
        }

        buffers.endBatch(lines);
    }
}
