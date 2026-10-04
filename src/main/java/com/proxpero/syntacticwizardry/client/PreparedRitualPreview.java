package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.BindingRitualStructure;
import com.proxpero.syntacticwizardry.ChalkRegistry;
import com.proxpero.syntacticwizardry.ChalkRuneBlock;
import com.proxpero.syntacticwizardry.EclipseRitualStructure;
import com.proxpero.syntacticwizardry.MagicLightRegistry;
import com.proxpero.syntacticwizardry.MooncallPhase;
import com.proxpero.syntacticwizardry.MooncallRitualStructure;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.RitualStructureRules;
import com.proxpero.syntacticwizardry.SummoningRitualStructure;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
            String label,
            BlockState previewState) {
        public GhostBlock(
                BlockPos pos,
                RitualStructureRules.Role role,
                RitualStructureRules.FocusMaterial focusMaterial,
                String label) {
            this(pos, role, focusMaterial, label, null);
        }
    }

    private static RitualDefinition ritual;
    private static BlockPos center;
    private static int requestedPotence;
    private static MooncallPhase mooncallPhase = MooncallPhase.FULL_MOON;
    private static RitualStructureRules.FocusPlan focusPlan;
    private static List<GhostBlock> ghosts = List.of();

    private PreparedRitualPreview() {}

    public static void prepare(RitualDefinition definition, BlockPos ritualCenter) {
        prepare(definition, ritualCenter, 1, MooncallPhase.FULL_MOON);
    }

    public static void prepare(RitualDefinition definition, BlockPos ritualCenter, int potence) {
        prepare(definition, ritualCenter, potence, MooncallPhase.FULL_MOON);
    }

    public static void prepare(
            RitualDefinition definition,
            BlockPos ritualCenter,
            int potence,
            MooncallPhase selectedMoonPhase) {
        if (definition == null || ritualCenter == null) return;
        ritual = definition;
        center = ritualCenter.immutable();
        mooncallPhase = selectedMoonPhase == null ? MooncallPhase.FULL_MOON : selectedMoonPhase;

        if (definition == RitualDefinition.MOONCALL
                || definition == RitualDefinition.SUMMONING
                || definition == RitualDefinition.BINDING
                || definition == RitualDefinition.PROTECTION) {
            requestedPotence = 0;
            focusPlan = null;
        } else {
            requestedPotence = Math.max(1, potence);
            focusPlan = RitualStructureRules.focusPlan(requestedPotence);
        }

        ghosts = previewGhosts(definition, center, requestedPotence, mooncallPhase);
    }

    public static List<GhostBlock> previewGhosts(
            RitualDefinition definition,
            BlockPos ritualCenter,
            int potence) {
        return previewGhosts(definition, ritualCenter, potence, MooncallPhase.FULL_MOON);
    }

    public static List<GhostBlock> previewGhosts(
            RitualDefinition definition,
            BlockPos ritualCenter,
            int potence,
            MooncallPhase selectedMoonPhase) {
        if (definition == null || ritualCenter == null) return List.of();

        BlockPos previewCenter = ritualCenter.immutable();
        RitualStructureRules.FocusPlan previewFocus =
                definition == RitualDefinition.MOONCALL
                        || definition == RitualDefinition.SUMMONING
                        || definition == RitualDefinition.BINDING
                        || definition == RitualDefinition.PROTECTION
                        ? null
                        : RitualStructureRules.focusPlan(Math.max(1, potence));
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
        } else if (definition == RitualDefinition.MOONCALL) {
            MooncallPhase phase = selectedMoonPhase == null
                    ? MooncallPhase.FULL_MOON
                    : selectedMoonPhase;
            built.add(new GhostBlock(
                    previewCenter.offset(MooncallRitualStructure.obsidianOffset(phase)),
                    RitualStructureRules.Role.STRUCTURAL,
                    null,
                    "Obsidian - " + phase.displayName()));
            for (BlockPos offset : EclipseRitualStructure.runeOffsets()) {
                built.add(new GhostBlock(
                        previewCenter.offset(offset),
                        RitualStructureRules.Role.RUNE,
                        null,
                        "Chalk Rune"));
            }
        } else if (definition == RitualDefinition.SUMMONING) {
            for (BlockPos offset : SummoningRitualStructure.runeOffsets()) {
                built.add(new GhostBlock(
                        previewCenter.offset(offset),
                        RitualStructureRules.Role.RUNE,
                        null,
                        "Chalk Rune"));
            }
            for (BlockPos offset : SummoningRitualStructure.structuralOffsets()) {
                built.add(new GhostBlock(
                        previewCenter.offset(offset),
                        RitualStructureRules.Role.STRUCTURAL,
                        null,
                        "Structural Block",
                        Blocks.STONE.defaultBlockState()));
            }
        } else if (definition == RitualDefinition.BINDING) {
            if (ChalkRegistry.block() != null) {
                for (BindingRitualStructure.RunePlacement placement : BindingRitualStructure.runes()) {
                    BlockState runeState = ChalkRegistry.block().defaultBlockState()
                            .setValue(ChalkRuneBlock.FACING, Direction.UP)
                            .setValue(ChalkRuneBlock.GLYPH, placement.glyph());
                    built.add(new GhostBlock(
                            previewCenter.offset(placement.offset()),
                            RitualStructureRules.Role.RUNE,
                            null,
                            "Chalk Rune " + placement.glyph(),
                            runeState));
                }
            }

            if (MagicLightRegistry.block() != null) {
                for (BlockPos offset : BindingRitualStructure.lightOffsets()) {
                    built.add(new GhostBlock(
                            previewCenter.offset(offset),
                            RitualStructureRules.Role.STRUCTURAL,
                            null,
                            "Magic Light",
                            MagicLightRegistry.block().defaultBlockState()));
                }
            }
        }

        if (previewFocus != null) {
            for (RitualStructureRules.FocusPlacement placement : previewFocus.placements()) {
                built.add(new GhostBlock(
                        placement.position(previewCenter),
                        RitualStructureRules.Role.FOCUS,
                        placement.material(),
                        placement.material().displayName() + " Focus"));
            }
        }

        ProtectionRitualPreviewSupport.append(definition, previewCenter, built);
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

    public static MooncallPhase mooncallPhase() {
        return mooncallPhase;
    }

    public static List<GhostBlock> ghosts() {
        return ghosts;
    }

    public static void clear() {
        ritual = null;
        center = null;
        requestedPotence = 0;
        mooncallPhase = MooncallPhase.FULL_MOON;
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
        RitualGhostBlockRenderer.render(event, blocks, alpha);
    }

}