package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record LuminalBridgeTravelPayload(UUID sourceId, UUID destinationId) implements CustomPacketPayload {
    public static final Type<LuminalBridgeTravelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "luminal_travel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LuminalBridgeTravelPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LuminalBridgeTravelPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            return new LuminalBridgeTravelPayload(raw.readUUID(), raw.readUUID());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LuminalBridgeTravelPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeUUID(payload.sourceId());
            raw.writeUUID(payload.destinationId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
