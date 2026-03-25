package com.redlimerl.speedrunigt.timer.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class TimerPacketUtils {

    @Environment(EnvType.CLIENT)
    public static void sendClient2ServerPacket(Minecraft client, TimerPacket<?> packet) {
        if (client.getConnection() != null) ClientPlayNetworking.send(packet);
    }

    public static void sendServer2ClientPacket(Collection<ServerPlayer> players, TimerPacket<?> packet) {
        for (ServerPlayer player : players) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket<?> packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(ServerPlayer player, TimerPacket<?> packet) {
        ServerPlayNetworking.send(player, packet);
    }
}
