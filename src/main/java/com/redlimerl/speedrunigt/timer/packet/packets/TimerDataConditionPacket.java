package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerDataConditionPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("condition_data");
    private final Integer sendKey;
    private final Integer sendValue;

    public TimerDataConditionPacket() {
        this(0, 0);
    }

    public TimerDataConditionPacket(Integer key, Integer value) {
        super(IDENTIFIER);
        this.sendKey = key;
        this.sendValue = value;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendKey != null) buf.writeInt(sendKey);
        if (sendValue != null) buf.writeInt(sendValue);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendKey != null) buf.writeInt(sendKey);
        if (sendValue != null) buf.writeInt(sendValue);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().updateMoreData(buf.readInt(), buf.readInt(), false);
    }
}
