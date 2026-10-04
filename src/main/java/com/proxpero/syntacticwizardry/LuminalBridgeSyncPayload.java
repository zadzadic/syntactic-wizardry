package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record LuminalBridgeSyncPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Entry(UUID id, String name, String dimension, long center, boolean paused, boolean powered) {}

    public static final Type<LuminalBridgeSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "luminal_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LuminalBridgeSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LuminalBridgeSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            int count = Math.min(512, Math.max(0, raw.readVarInt()));
            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(
                        raw.readUUID(),
                        raw.readUtf(32),
                        raw.readUtf(128),
                        raw.readLong(),
                        raw.readBoolean(),
                        raw.readBoolean()));
            }
            return new LuminalBridgeSyncPayload(List.copyOf(entries));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LuminalBridgeSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeVarInt(payload.entries().size());
            for (Entry entry : payload.entries()) {
                raw.writeUUID(entry.id());
                raw.writeUtf(entry.name(), 32);
                raw.writeUtf(entry.dimension(), 128);
                raw.writeLong(entry.center());
                raw.writeBoolean(entry.paused());
                raw.writeBoolean(entry.powered());
            }
        }
    };

    public LuminalBridgeSyncPayload {
        entries = List.copyOf(entries == null ? List.of() : entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
