package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public class TimerDataConditionPacket extends TimerPacket<TimerDataConditionPacket> {

    public static final CustomPacketPayload.Type<TimerDataConditionPacket> IDENTIFIER = TimerPacket.identifier("condition_data");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerDataConditionPacket> CODEC = TimerPacket.codecOf(TimerDataConditionPacket::write, TimerDataConditionPacket::new);
    private final int sendKey;
    private final int sendValue;

    public TimerDataConditionPacket(int key, int value) {
        super(IDENTIFIER);
        this.sendKey = key;
        this.sendValue = value;
    }

    public TimerDataConditionPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
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
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().updateMoreData(this.sendKey, this.sendValue, false);
    }
}
