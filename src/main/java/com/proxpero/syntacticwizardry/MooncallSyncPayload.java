package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MooncallSyncPayload(long syncGameTime, List<Entry> entries) implements CustomPacketPayload {
    public record Entry(UUID id, String name, long center, int phase, int effectTicks,
                        boolean paused, boolean powered, boolean stopping, boolean effectApplied) {}

    public static final Type<MooncallSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "mooncall_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MooncallSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MooncallSyncPayload decode(RegistryFriendlyByteBuf buffer) {
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
                        raw.readBoolean(),
                        raw.readBoolean(),
                        raw.readBoolean()));
            }
            return new MooncallSyncPayload(syncTime, List.copyOf(entries));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, MooncallSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeLong(payload.syncGameTime());
            raw.writeVarInt(payload.entries().size());
            for (Entry entry : payload.entries()) {
                raw.writeUUID(entry.id());
                raw.writeUtf(entry.name(), 32);
                raw.writeLong(entry.center());
                raw.writeVarInt(entry.phase());
                raw.writeVarInt(entry.effectTicks());
                raw.writeBoolean(entry.paused());
                raw.writeBoolean(entry.powered());
                raw.writeBoolean(entry.stopping());
                raw.writeBoolean(entry.effectApplied());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
