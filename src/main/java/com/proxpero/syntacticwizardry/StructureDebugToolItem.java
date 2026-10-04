package com.proxpero.syntacticwizardry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = SyntacticWizardry.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class StructureDebugToolItem extends Item {
    private static final Map<UUID, BlockPos> FIRST_CORNERS = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public StructureDebugToolItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(SyntacticWizardry.STRUCTURE_DEBUG_TOOL.get())) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!player.getAbilities().instabuild) {
            player.displayClientMessage(
                    Component.literal("Structure Debug Tool is Creative-only."),
                    true);
            return;
        }

        BlockPos clicked = event.getPos().immutable();
        BlockPos first = FIRST_CORNERS.remove(player.getUUID());

        if (first == null) {
            FIRST_CORNERS.put(player.getUUID(), clicked);
            player.displayClientMessage(
                    Component.literal("Structure corner 1: " + formatPos(clicked) + ". Select the opposite corner."),
                    true);
            return;
        }

        try {
            ExportResult result = export(level, first, clicked);
            player.displayClientMessage(
                    Component.literal(
                            "Saved Ritual Pattern: "
                                    + result.file().getFileName()
                                    + " ("
                                    + result.blockCount()
                                    + " blocks, "
                                    + result.width()
                                    + "x"
                                    + result.height()
                                    + "x"
                                    + result.depth()
                                    + ")."),
                    false);
        } catch (IOException exception) {
            player.displayClientMessage(
                    Component.literal("Failed to save Ritual Pattern: " + exception.getMessage()),
                    false);
        }
    }

    private static ExportResult export(ServerLevel level, BlockPos first, BlockPos second) throws IOException {
        int minX = Math.min(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxX = Math.max(first.getX(), second.getX());
        int maxY = Math.max(first.getY(), second.getY());
        int maxZ = Math.max(first.getZ(), second.getZ());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;

        JsonObject root = new JsonObject();
        root.addProperty("format", "syntacticwizardry_ritual_pattern_v1");
        root.addProperty("dimension", level.dimension().location().toString());

        JsonObject dimensions = new JsonObject();
        dimensions.addProperty("width", width);
        dimensions.addProperty("height", height);
        dimensions.addProperty("depth", depth);
        root.add("dimensions", dimensions);

        JsonObject selection = new JsonObject();
        selection.add("min", positionArray(minX, minY, minZ));
        selection.add("max", positionArray(maxX, maxY, maxZ));
        root.add("selection", selection);

        JsonArray blocks = new JsonArray();
        int blockCount = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (ignored(state)) continue;

                    JsonObject block = new JsonObject();
                    block.addProperty("x", x - minX);
                    block.addProperty("y", y - minY);
                    block.addProperty("z", z - minZ);
                    block.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());

                    if (!state.getValues().isEmpty()) {
                        JsonObject properties = new JsonObject();
                        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
                            properties.addProperty(
                                    entry.getKey().getName(),
                                    propertyValue(entry.getKey(), entry.getValue()));
                        }
                        block.add("properties", properties);
                    }

                    blocks.add(block);
                    blockCount++;
                }
            }
        }

        root.add("blocks", blocks);

        Path directory = FMLPaths.GAMEDIR.get().resolve("Ritual Patterns");
        Files.createDirectories(directory);

        String baseName = "ritual_pattern_" + FILE_TIME.format(LocalDateTime.now());
        Path file = availableFile(directory, baseName);

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }

        return new ExportResult(file, blockCount, width, height, depth);
    }

    private static boolean ignored(BlockState state) {
        return state.isAir()
                || state.is(Blocks.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS);
    }

    private static JsonArray positionArray(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValue(Property property, Comparable value) {
        return property.getName(value);
    }

    private static Path availableFile(Path directory, String baseName) {
        Path candidate = directory.resolve(baseName + ".json");
        if (!Files.exists(candidate)) return candidate;

        int index = 2;
        while (true) {
            candidate = directory.resolve(baseName + "_" + index + ".json");
            if (!Files.exists(candidate)) return candidate;
            index++;
        }
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private record ExportResult(Path file, int blockCount, int width, int height, int depth) {}
}
