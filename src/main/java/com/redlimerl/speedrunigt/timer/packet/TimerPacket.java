package com.redlimerl.speedrunigt.timer.packet;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class TimerPacket {

    private static final HashMap<String, Supplier<? extends TimerPacket>> registered = Maps.newHashMap();
    static void registryPacket(Identifier identifier, Supplier<? extends TimerPacket> packet) {
        registered.put(identifier.toString(), packet);
    }
    public static Identifier identifier(String id) {
        return new Identifier(SpeedRunIGT.MOD_ID, id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TimerPacket> T createTimerPacketFromPacket(Identifier identifier) {
        if (registered.containsKey(identifier.toString())) {
            try {
                TimerPacket packet = registered.get(identifier.toString()).get();
                return (T) packet;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private final Identifier identifier;

    public TimerPacket(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    @Environment(EnvType.CLIENT)
    final CustomPayloadC2SPacket createClient2ServerPacket(MinecraftClient client) {
        TimerPacketBuf buf = TimerPacketBuf.create();
        return new CustomPayloadC2SPacket(this.identifier, this.convertClient2ServerPacket(buf, client).getBuffer());
    }

    final CustomPayloadS2CPacket createServer2ClientPacket(MinecraftServer server, TimerPacketBuf buf) {
        return new CustomPayloadS2CPacket(this.identifier, this.convertServer2ClientPacket(buf.copy(), server).getBuffer());
    }

    final CustomPayloadS2CPacket createServer2ClientPacket(MinecraftServer server) {
        TimerPacketBuf buf = TimerPacketBuf.create();
        return this.createServer2ClientPacket(server, buf);
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