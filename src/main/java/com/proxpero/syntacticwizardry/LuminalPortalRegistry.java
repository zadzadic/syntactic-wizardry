package com.proxpero.syntacticwizardry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class LuminalPortalRegistry {
    private static Block BLOCK;

    private LuminalPortalRegistry() {}

    public static Block block() {
        return BLOCK;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "luminal_portal");
        event.register(Registries.BLOCK, id, () -> {
            BLOCK = new LuminalPortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL)
                    .noLootTable()
                    .strength(-1.0F, 3600000.0F));
            return BLOCK;
        });
    }
}
