package com.redlimerl.speedrunigt.timer.packet;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class TimerPacket {

    private static final HashMap<String, Supplier<? extends TimerPacket>> registered = Maps.newHashMap();
    static void registryPacket(String identifier, Supplier<? extends TimerPacket> packet) {
        registered.put(identifier, packet);
    }
    public static String identifier(String id) {
        return "srigt|" + id;

    }

    @SuppressWarnings("unchecked")
    public static <T extends TimerPacket> T createTimerPacketFromPacket(String identifier) {
        if (registered.containsKey(identifier)) {
            try {
                TimerPacket packet = registered.get(identifier).get();
                return (T) packet;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private final String identifier;

    public TimerPacket(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Environment(EnvType.CLIENT)
    final CustomPayloadC2SPacket createClient2ServerPacket(MinecraftClient client) {
        TimerPacketBuf buf = TimerPacketBuf.create();
        return new CustomPayloadC2SPacket(identifier, convertClient2ServerPacket(buf, client).getBuffer());
    }

    final CustomPayloadS2CPacket createServer2ClientPacket(MinecraftServer server, TimerPacketBuf buf) {
        return new CustomPayloadS2CPacket(identifier, convertServer2ClientPacket(buf.copy(), server).getBuffer());
    }

    final CustomPayloadS2CPacket createServer2ClientPacket(MinecraftServer server) {
        TimerPacketBuf buf = TimerPacketBuf.create();
        return createServer2ClientPacket(server, buf);
    }

    protected void sendPacketToPlayers(TimerPacketBuf buf, MinecraftServer server) {
        TimerPacketUtils.sendServer2ClientPacket(server, this, buf);
    }

    @Environment(EnvType.CLIENT)
    protected abstract TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client);

    public abstract void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server);

    protected abstract TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server);

    @Environment(EnvType.CLIENT)
    public abstract void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client);
}