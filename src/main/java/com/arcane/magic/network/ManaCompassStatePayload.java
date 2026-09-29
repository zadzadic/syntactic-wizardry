package com.arcane.magic.network;

import com.proxpero.syntacticwizardry.LegacyBuilderBridge;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Compatibility payload used by the exact Arcane 0.4.267 Builder inventory/fluid panel. */
public record ManaCompassStatePayload(String data) implements CustomPacketPayload {
    public static final Type<ManaCompassStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "legacy_builder_state_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaCompassStatePayload> STREAM_CODEC = StreamCodec.composite(
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8,
            ManaCompassStatePayload::data,
            ManaCompassStatePayload::new
    );

    public static void handle(ManaCompassStatePayload payload, IPayloadContext context) {
        LegacyBuilderBridge.invoke("com.arcane.magic.builder.BuilderInventoryPanel", "handleState", payload.data());
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
