package com.redlimerl.speedrunigt.timer.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class TimerPacketUtils {

    @Environment(EnvType.CLIENT)
    public static void sendClient2ServerPacket(MinecraftClient client, TimerPacket<?> packet) {
        if (client.getNetworkHandler() != null) ClientPlayNetworking.send(packet);
    }

    public static void sendServer2ClientPacket(Collection<ServerPlayerEntity> players, TimerPacket<?> packet) {
        for (ServerPlayerEntity player : players) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket<?> packet) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(ServerPlayerEntity player, TimerPacket<?> packet) {
        ServerPlayNetworking.send(player, packet);
    }
}
