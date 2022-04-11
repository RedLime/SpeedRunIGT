package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public class TimerDataConditionPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("co_da");
    private final Integer sendKey;
    private final Integer sendValue;

    public TimerDataConditionPacket() {
        this(null, null);
    }

    public TimerDataConditionPacket(Integer key, Integer value) {
        super(IDENTIFIER);
        this.sendKey = key;
        this.sendValue = value;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendKey != null) buf.writeInt(sendKey);
        if (sendValue != null) buf.writeInt(sendValue);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            InGameTimer.getInstance().updateMoreData(copiedBuf.readInt(), copiedBuf.readInt(), false);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendKey != null) buf.writeInt(sendKey);
        if (sendValue != null) buf.writeInt(sendValue);
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().updateMoreData(buf.readInt(), buf.readInt(), false);
    }
}
