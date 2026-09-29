package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FocusSlotCyclePayload(int delta) implements CustomPacketPayload {
    public static final Type<FocusSlotCyclePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "focus_slot_cycle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FocusSlotCyclePayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public FocusSlotCyclePayload decode(RegistryFriendlyByteBuf buffer) { return new FocusSlotCyclePayload(buffer.readByte()); }
        @Override public void encode(RegistryFriendlyByteBuf buffer, FocusSlotCyclePayload payload) { buffer.writeByte(payload.delta()); }
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
