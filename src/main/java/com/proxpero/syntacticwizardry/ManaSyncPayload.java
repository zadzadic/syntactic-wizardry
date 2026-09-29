package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ManaSyncPayload(float mana) implements CustomPacketPayload {
    public static final Type<ManaSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "mana_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public ManaSyncPayload decode(RegistryFriendlyByteBuf buffer) {
            FriendlyByteBuf raw = buffer;
            return new ManaSyncPayload(raw.readFloat());
        }
        @Override public void encode(RegistryFriendlyByteBuf buffer, ManaSyncPayload payload) {
            FriendlyByteBuf raw = buffer;
            raw.writeFloat(payload.mana());
        }
    };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
