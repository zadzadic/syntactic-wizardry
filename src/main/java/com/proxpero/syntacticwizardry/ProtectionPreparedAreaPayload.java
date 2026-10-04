package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProtectionPreparedAreaPayload(long center, int shape,
                                            int minX, int minY, int minZ,
                                            int maxX, int maxY, int maxZ,
                                            boolean hasArea) implements CustomPacketPayload {
    public static final Type<ProtectionPreparedAreaPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "protection_prepared_area"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProtectionPreparedAreaPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ProtectionPreparedAreaPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            return new ProtectionPreparedAreaPayload(
                    raw.readLong(), raw.readVarInt(),
                    raw.readInt(), raw.readInt(), raw.readInt(),
                    raw.readInt(), raw.readInt(), raw.readInt(),
                    raw.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ProtectionPreparedAreaPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeLong(payload.center());
            raw.writeVarInt(payload.shape());
            raw.writeInt(payload.minX());
            raw.writeInt(payload.minY());
            raw.writeInt(payload.minZ());
            raw.writeInt(payload.maxX());
            raw.writeInt(payload.maxY());
            raw.writeInt(payload.maxZ());
            raw.writeBoolean(payload.hasArea());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}