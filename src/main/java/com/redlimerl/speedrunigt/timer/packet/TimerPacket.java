package com.redlimerl.speedrunigt.timer.packet;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.function.Supplier;

public abstract class TimerPacket implements CustomPayload {
    public enum Side {
        SERVER,
        CLIENT
    }

    private static final HashMap<String, Supplier<? extends TimerPacket>> registered = Maps.newHashMap();

    public static <B, V> PacketCodec<B, V> codecOf(ValueFirstEncoder<B, V> encoder, PacketDecoder<B, V> decoder) {
        return PacketCodec.of(encoder, decoder);
    }

    static void registryPacket(CustomPayload.Id<CustomPayload> identifier, Supplier<? extends TimerPacket> packet, Side side) {
        switch (side) {
            case SERVER -> {
                registered.put(identifier.toString(), packet);
                registryPacketServer(identifier);
            } case CLIENT -> {
                registryPacketClient(identifier);
            }
        }
    }

    static void registryPacketClient(CustomPayload.Id<CustomPayload> identifier) {
//        ClientPlayNetworking.registerGlobalReceiver(identifier, (payload, context) -> {
//            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(identifier);
//            TimerPacketBuf timerPacketBuf = TimerPacketBuf.of(payload);
//            if (timerPacket != null && SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) {
//                timerPacket.receiveServer2ClientPacket(timerPacketBuf, context.client());
//            }
//        });
    }

    static void registryPacketServer(CustomPayload.Id<CustomPayload> identifier) {
//        ServerPlayNetworking.registerGlobalReceiver(identifier, (payload, context) -> {
//            TimerPacket timerPacket = TimerPacket.createTimerPacketFromPacket(identifier);
//            TimerPacketBuf timerPacketBuf = TimerPacketBuf.of(payload);
//            if (timerPacket != null) {
//                timerPacket.receiveClient2ServerPacket(timerPacketBuf, context.player().getServer());
//            }
//        });
    }

    public static CustomPayload.Id<CustomPayload> identifier(String id) {
        return CustomPayload.id(SpeedRunIGT.MOD_ID + ":" + id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TimerPacket> T createTimerPacketFromPacket(CustomPayload.Id<CustomPayload> identifier) {
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

    private final CustomPayload.Id<CustomPayload> identifier;

    public TimerPacket(CustomPayload.Id<CustomPayload> identifier) {
        this.identifier = identifier;
    }

    public CustomPayload.Id<CustomPayload> getIdentifier() {
        return this.identifier;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return this.getIdentifier();
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