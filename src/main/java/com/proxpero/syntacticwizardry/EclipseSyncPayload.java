package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record EclipseSyncPayload(long syncGameTime, List<Entry> entries) implements CustomPacketPayload {
    public record Entry(UUID id, String name, long center, int potence, int transitionTicks,
                        boolean paused, boolean powered) {}

    public static final Type<EclipseSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "eclipse_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EclipseSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EclipseSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            long syncTime = raw.readLong();
            int count = Math.min(256, Math.max(0, raw.readVarInt()));
            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(
                        raw.readUUID(),
                        raw.readUtf(32),
                        raw.readLong(),
                        raw.readVarInt(),
                        raw.readVarInt(),
                        raw.readBoolean(),
                        raw.readBoolean()));
            }
            return new EclipseSyncPayload(syncTime, List.copyOf(entries));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, EclipseSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeLong(payload.syncGameTime());
            raw.writeVarInt(payload.entries().size());
            for (Entry entry : payload.entries()) {
                raw.writeUUID(entry.id());
                raw.writeUtf(entry.name(), 32);
                raw.writeLong(entry.center());
                raw.writeVarInt(entry.potence());
                raw.writeVarInt(entry.transitionTicks());
                raw.writeBoolean(entry.paused());
                raw.writeBoolean(entry.powered());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
