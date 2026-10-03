package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RitualControlPayload(UUID id, String action, String value) implements CustomPacketPayload {
    public static final Type<RitualControlPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "ritual_control"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualControlPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RitualControlPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            return new RitualControlPayload(raw.readUUID(), raw.readUtf(24), raw.readUtf(32));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, RitualControlPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeUUID(payload.id());
            raw.writeUtf(payload.action(), 24);
            raw.writeUtf(payload.value(), 32);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
