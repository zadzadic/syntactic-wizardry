package com.proxpero.syntacticwizardry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PermanencyPreparedConfigPayload(
        long center,
        int shape,
        int minX, int minY, int minZ,
        int maxX, int maxY, int maxZ,
        float yaw,
        float pitch,
        int[] plan,
        int[] settings,
        boolean hasConfig) implements CustomPacketPayload {

    public static final Type<PermanencyPreparedConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    SyntacticWizardry.MOD_ID, "permanency_prepared_config"));

    public PermanencyPreparedConfigPayload {
        plan = plan == null ? new int[0] : plan.clone();
        settings = settings == null ? new int[0] : settings.clone();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PermanencyPreparedConfigPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public PermanencyPreparedConfigPayload decode(RegistryFriendlyByteBuf buffer) {
                    FriendlyByteBuf raw = buffer;
                    long center = raw.readLong();
                    int shape = raw.readVarInt();
                    int minX = raw.readInt();
                    int minY = raw.readInt();
                    int minZ = raw.readInt();
                    int maxX = raw.readInt();
                    int maxY = raw.readInt();
                    int maxZ = raw.readInt();
                    float yaw = raw.readFloat();
                    float pitch = raw.readFloat();

                    int encodedPlanLength = Math.max(0, raw.readVarInt());
                    int planLength = Math.min(SpellPresentation.PLAN_DATA_SIZE, encodedPlanLength);
                    int[] plan = new int[planLength];
                    for (int i = 0; i < encodedPlanLength; i++) {
                        int value = raw.readInt();
                        if (i < planLength) plan[i] = value;
                    }

                    int encodedSettingsLength = Math.max(0, raw.readVarInt());
                    int settingsLength = Math.min(SpellPresentation.SETTINGS_DATA_SIZE, encodedSettingsLength);
                    int[] settings = new int[settingsLength];
                    for (int i = 0; i < encodedSettingsLength; i++) {
                        int value = raw.readInt();
                        if (i < settingsLength) settings[i] = value;
                    }

                    boolean hasConfig = raw.readBoolean();
                    return new PermanencyPreparedConfigPayload(
                            center, shape,
                            minX, minY, minZ, maxX, maxY, maxZ,
                            yaw, pitch, plan, settings, hasConfig);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, PermanencyPreparedConfigPayload payload) {
                    FriendlyByteBuf raw = buffer;
                    raw.writeLong(payload.center());
                    raw.writeVarInt(payload.shape());
                    raw.writeInt(payload.minX());
                    raw.writeInt(payload.minY());
                    raw.writeInt(payload.minZ());
                    raw.writeInt(payload.maxX());
                    raw.writeInt(payload.maxY());
                    raw.writeInt(payload.maxZ());
                    raw.writeFloat(payload.yaw());
                    raw.writeFloat(payload.pitch());

                    int[] plan = payload.plan();
                    raw.writeVarInt(plan.length);
                    for (int value : plan) raw.writeInt(value);

                    int[] settings = payload.settings();
                    raw.writeVarInt(settings.length);
                    for (int value : settings) raw.writeInt(value);

                    raw.writeBoolean(payload.hasConfig());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
