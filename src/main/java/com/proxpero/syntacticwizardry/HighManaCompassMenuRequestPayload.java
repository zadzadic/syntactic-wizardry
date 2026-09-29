package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HighManaCompassMenuRequestPayload() implements CustomPacketPayload {
    public static final HighManaCompassMenuRequestPayload INSTANCE = new HighManaCompassMenuRequestPayload();
    public static final Type<HighManaCompassMenuRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "high_mana_compass_menu_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HighManaCompassMenuRequestPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public HighManaCompassMenuRequestPayload decode(RegistryFriendlyByteBuf buffer) { return INSTANCE; }
        @Override public void encode(RegistryFriendlyByteBuf buffer, HighManaCompassMenuRequestPayload payload) {}
    };
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
