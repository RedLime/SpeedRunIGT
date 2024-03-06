package com.redlimerl.speedrunigt.timer.packet;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;
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
        return this.identifier;
    }

    @Environment(EnvType.CLIENT)
    final CustomPayloadC2SPacket createClient2ServerPacket(Minecraft client) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.convertClient2ServerPacket(new DataOutputStream(buf), client);
        return new CustomPayloadC2SPacket(this.identifier, buf.toByteArray());
    }

    final CustomPayloadC2SPacket createServer2ClientPacket(MinecraftServer server, byte[] bytes) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(bytes);
        this.convertServer2ClientPacket(new DataOutputStream(buf), server);
        return new CustomPayloadC2SPacket(this.identifier, buf.toByteArray());
    }

    final CustomPayloadC2SPacket createServer2ClientPacket(MinecraftServer server) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.convertServer2ClientPacket(new DataOutputStream(buf), server);
        return new CustomPayloadC2SPacket(this.identifier, buf.toByteArray());
    }


    protected void sendPacketToPlayers(byte[] bytes, MinecraftServer server) throws IOException {
        TimerPacketUtils.sendServer2ClientPacket(server, this, bytes);
    }

    @Environment(EnvType.CLIENT)
    protected abstract void convertClient2ServerPacket(DataOutputStream buf, Minecraft client) throws IOException;

    public abstract void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException;

    protected abstract void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException;

    public abstract void receiveServer2ClientPacket(DataInputStream buf, Minecraft client) throws IOException;
}