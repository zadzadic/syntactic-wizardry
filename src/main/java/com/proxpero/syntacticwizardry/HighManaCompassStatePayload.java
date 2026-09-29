package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HighManaCompassStatePayload(boolean hasTarget, String dimension, int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final Type<HighManaCompassStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "high_mana_compass_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HighManaCompassStatePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public HighManaCompassStatePayload decode(RegistryFriendlyByteBuf buffer) {
            return new HighManaCompassStatePayload(buffer.readBoolean(), buffer.readUtf(), buffer.readInt(), buffer.readInt());
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, HighManaCompassStatePayload payload) {
            buffer.writeBoolean(payload.hasTarget());
            buffer.writeUtf(payload.dimension());
            buffer.writeInt(payload.chunkX());
            buffer.writeInt(payload.chunkZ());
        }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
