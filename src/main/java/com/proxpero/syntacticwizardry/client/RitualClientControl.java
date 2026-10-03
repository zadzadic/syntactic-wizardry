package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RitualControlPayload;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public final class RitualClientControl {
    private RitualClientControl() {}

    public static void pause(UUID id, boolean paused) {
        PacketDistributor.sendToServer(new RitualControlPayload(id, paused ? "pause" : "resume", ""));
    }

    public static void stop(UUID id) {
        PacketDistributor.sendToServer(new RitualControlPayload(id, "stop", ""));
    }

    public static void rename(UUID id, String name) {
        PacketDistributor.sendToServer(new RitualControlPayload(id, "rename", name == null ? "" : name));
    }
}
