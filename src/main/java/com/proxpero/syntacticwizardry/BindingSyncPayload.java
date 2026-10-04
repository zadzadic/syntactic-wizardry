package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BindingSyncPayload(long syncGameTime, List<Entry> entries) implements CustomPacketPayload {
    public record Entry(UUID id, String name, long center, int effectTicks,
                        boolean paused, boolean powered, boolean stopping) {}

    public static final Type<BindingSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "binding_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindingSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BindingSyncPayload decode(RegistryFriendlyByteBuf buffer) {
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
                        raw.readBoolean(),
                        raw.readBoolean(),
                        raw.readBoolean()));
            }

            return new BindingSyncPayload(syncTime, List.copyOf(entries));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, BindingSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeLong(payload.syncGameTime());
            raw.writeVarInt(payload.entries().size());

            for (Entry entry : payload.entries()) {
                raw.writeUUID(entry.id());
                raw.writeUtf(entry.name(), 32);
                raw.writeLong(entry.center());
                raw.writeVarInt(entry.effectTicks());
                raw.writeBoolean(entry.paused());
                raw.writeBoolean(entry.powered());
                raw.writeBoolean(entry.stopping());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
