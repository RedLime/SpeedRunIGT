package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public abstract class TimerPacket<T extends CustomPayload> implements CustomPayload {

    public static <B, V> PacketCodec<B, V> codecOf(ValueFirstEncoder<B, V> encoder, PacketDecoder<B, V> decoder) {
        return PacketCodec.of(encoder, decoder);
    }

    public static <T extends CustomPayload> CustomPayload.Id<T> identifier(String id) {
        return new CustomPayload.Id<>(Identifier.of(SpeedRunIGT.MOD_ID, id));
    }

    private final CustomPayload.Id<T> identifier;

    public TimerPacket(CustomPayload.Id<T> identifier) {
        this.identifier = identifier;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return this.identifier;
    }

    protected void sendPacketToPlayers(MinecraftServer server) {
        TimerPacketUtils.sendServer2ClientPacket(server, this);
    }

    protected abstract void write(RegistryByteBuf buf);

    public abstract void receiveClient2ServerPacket(MinecraftServer server);

    @Environment(EnvType.CLIENT)
    public abstract void receiveServer2ClientPacket(MinecraftClient client);
}