package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightSyncPayload(int potence) implements CustomPacketPayload {
    public static final Type<FlightSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "flight_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlightSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public FlightSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            return new FlightSyncPayload(raw.readVarInt());
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, FlightSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeVarInt(payload.potence());
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
