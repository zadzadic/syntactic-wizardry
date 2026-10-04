package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record LuminalBridgeDestinationsPayload(UUID sourceId, List<Destination> destinations)
        implements CustomPacketPayload {

    public record Destination(UUID id, String label, String dimension, int x, int y, int z) {}

    public static final Type<LuminalBridgeDestinationsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "luminal_destinations"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LuminalBridgeDestinationsPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public LuminalBridgeDestinationsPayload decode(RegistryFriendlyByteBuf buffer) {
                    FriendlyByteBuf raw = buffer;
                    UUID source = raw.readUUID();
                    int count = Math.min(512, Math.max(0, raw.readVarInt()));
                    List<Destination> destinations = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        destinations.add(new Destination(
                                raw.readUUID(),
                                raw.readUtf(64),
                                raw.readUtf(128),
                                raw.readInt(),
                                raw.readInt(),
                                raw.readInt()));
                    }
                    return new LuminalBridgeDestinationsPayload(source, List.copyOf(destinations));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, LuminalBridgeDestinationsPayload payload) {
                    FriendlyByteBuf raw = buffer;
                    raw.writeUUID(payload.sourceId());
                    raw.writeVarInt(payload.destinations().size());
                    for (Destination destination : payload.destinations()) {
                        raw.writeUUID(destination.id());
                        raw.writeUtf(destination.label(), 64);
                        raw.writeUtf(destination.dimension(), 128);
                        raw.writeInt(destination.x());
                        raw.writeInt(destination.y());
                        raw.writeInt(destination.z());
                    }
                }
            };

    public LuminalBridgeDestinationsPayload {
        destinations = List.copyOf(destinations == null ? List.of() : destinations);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
