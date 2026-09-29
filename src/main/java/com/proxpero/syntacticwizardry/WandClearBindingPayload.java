package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WandClearBindingPayload(boolean mainHand) implements CustomPacketPayload {
    public static final Type<WandClearBindingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "wand_clear_binding"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WandClearBindingPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public WandClearBindingPayload decode(RegistryFriendlyByteBuf buffer) { return new WandClearBindingPayload(buffer.readBoolean()); }
        @Override public void encode(RegistryFriendlyByteBuf buffer, WandClearBindingPayload payload) { buffer.writeBoolean(payload.mainHand()); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
