package com.arcane.magic.network;

import com.proxpero.syntacticwizardry.LegacyBuilderBridge;
import com.proxpero.syntacticwizardry.SyntacticWizardry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Exact command transport expected by the Arcane 0.4.267 Builder client code. */
public record ManaCompassCommandPayload(String command) implements CustomPacketPayload {
    public static final Type<ManaCompassCommandPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SyntacticWizardry.MOD_ID, "legacy_builder_command_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaCompassCommandPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ManaCompassCommandPayload::command, ManaCompassCommandPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ManaCompassCommandPayload payload, IPayloadContext context) {
        LegacyBuilderBridge.invoke("com.arcane.magic.builder.BuilderServerCommands", "handle", context.player(), payload.command());
    }
}
