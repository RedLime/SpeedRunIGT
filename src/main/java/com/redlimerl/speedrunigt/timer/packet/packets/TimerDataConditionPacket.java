package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

public class TimerDataConditionPacket extends TimerPacket<TimerDataConditionPacket> {

    public static final CustomPayload.Id<TimerDataConditionPacket> IDENTIFIER = TimerPacket.identifier("condition_data");
    public static final PacketCodec<RegistryByteBuf, TimerDataConditionPacket> CODEC = TimerPacket.codecOf(TimerDataConditionPacket::write, TimerDataConditionPacket::new);
    private final int sendKey;
    private final int sendValue;

    public TimerDataConditionPacket(int key, int value) {
        super(IDENTIFIER);
        this.sendKey = key;
        this.sendValue = value;
    }

    public TimerDataConditionPacket(RegistryByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    @Override
    protected void write(RegistryByteBuf buf) {
        buf.writeInt(this.sendKey);
        buf.writeInt(this.sendValue);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.getInstance().updateMoreData(this.sendKey, this.sendValue, false);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().updateMoreData(this.sendKey, this.sendValue, false);
    }
}
