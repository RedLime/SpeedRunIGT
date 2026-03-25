package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

public abstract class TimerPacket<T extends CustomPacketPayload> implements CustomPacketPayload {

    public static <B, V> StreamCodec<B, V> codecOf(StreamMemberEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return StreamCodec.ofMember(encoder, decoder);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> identifier(String id) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SpeedRunIGT.MOD_ID, id));
    }

    private final CustomPacketPayload.Type<T> identifier;

    public TimerPacket(CustomPacketPayload.Type<T> identifier) {
        this.identifier = identifier;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return this.identifier;
    }

    protected void sendPacketToPlayers(MinecraftServer server) {
        TimerPacketUtils.sendServer2ClientPacket(server, this);
    }

    protected abstract void write(RegistryFriendlyByteBuf buf);

    public abstract void receiveClient2ServerPacket(MinecraftServer server);

    @Environment(EnvType.CLIENT)
    public abstract void receiveServer2ClientPacket(Minecraft client);
}