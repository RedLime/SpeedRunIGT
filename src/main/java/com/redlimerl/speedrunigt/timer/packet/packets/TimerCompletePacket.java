package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public class TimerCompletePacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_co");
    private final Long endTime;

    public TimerCompletePacket() {
        this(null);
    }

    public TimerCompletePacket(Long time) {
        super(IDENTIFIER);
        this.endTime = time;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (endTime != null) buf.writeLong(endTime);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            InGameTimer.complete(copiedBuf.readLong(), false);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (endTime != null) buf.writeLong(endTime);
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.complete(buf.readLong(), false);
    }
}
