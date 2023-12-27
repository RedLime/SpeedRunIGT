package com.redlimerl.speedrunigt.timer.packet;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class TimerPacket {
    public enum Side {
        SERVER,
        CLIENT
    }

    private static final HashMap<String, Supplier<? extends TimerPacket>> registered = Maps.newHashMap();
    static void registryPacket(Identifier identifier, Supplier<? extends TimerPacket> packet, Side side) {
        switch (side) {
            case SERVER -> {
                registered.put(identifier.toString(), packet);
                registryPacketServer(identifier);
            } case CLIENT -> {
                registryPacketClient(identifier);
            }
        }
    }

    static void registryPacketClient(Identifier identifier) {
        ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, buf, responseSender) -> {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(identifier);
            TimerPacketBuf timerPacketBuf = TimerPacketBuf.of(buf);
            if (timerPacket != null && SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) {
                timerPacket.receiveServer2ClientPacket(timerPacketBuf, client);
            }
        });
    }

    static void registryPacketServer(Identifier identifier) {
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buf, responseSender) -> {
            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(identifier);
            TimerPacketBuf timerPacketBuf = TimerPacketBuf.of(buf);
            if (timerPacket != null) {
                timerPacket.receiveClient2ServerPacket(timerPacketBuf, server);
            }
        });
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
        return identifier;
    }

    @Environment(EnvType.CLIENT)
    final PacketByteBuf createClient2ServerPacket(MinecraftClient client) {
        TimerPacketBuf buf = TimerPacketBuf.create();
        return convertClient2ServerPacket(buf, client).getBuffer();
    }

    final PacketByteBuf createServer2ClientPacket(MinecraftServer server, TimerPacketBuf buf) {
        return convertServer2ClientPacket(buf.copy(), server).getBuffer();
    }

    final PacketByteBuf createServer2ClientPacket(MinecraftServer server) {
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