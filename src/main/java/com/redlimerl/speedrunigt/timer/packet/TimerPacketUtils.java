package com.redlimerl.speedrunigt.timer.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.util.Collection;

public class TimerPacketUtils {

    @Environment(EnvType.CLIENT)
    public static void sendClient2ServerPacket(Minecraft client, TimerPacket packet) {
        if (client.playerEntity != null && client.playerEntity.field_1667 != null) {
            try {
                client.playerEntity.field_1667.sendPacket(packet.createClient2ServerPacket(client));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sendServer2ClientPacket(ServerPlayerEntity player, TimerPacket packet) throws IOException {
        player.field_2823.sendPacket(packet.createServer2ClientPacket(player.server));
    }

    public static void sendServer2ClientPacket(Collection<ServerPlayerEntity> players, TimerPacket packet) throws IOException {
        for (ServerPlayerEntity player : players) sendServer2ClientPacket(player, packet);
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket packet) {
        try {
            sendServer2ClientPacket(server.getPlayerManager().players, packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendServer2ClientPacket(MinecraftServer server, TimerPacket packet, byte[] bytes) throws IOException {
        for (Object obj : server.getPlayerManager().players) {
            ((ServerPlayerEntity) obj).field_2823.sendPacket(packet.createServer2ClientPacket(server, bytes));
        }
    }
}
