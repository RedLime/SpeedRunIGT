package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerTimelinePacket extends TimerPacket {
    public static final Identifier IDENTIFIER = TimerPacket.identifier("timer_timeline");
    private final String sendTimeline;

    public TimerTimelinePacket() {
        this("");
    }

    public TimerTimelinePacket(String timeline) {
        super(IDENTIFIER);
        this.sendTimeline = timeline;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (this.sendTimeline != null) buf.writeString(this.sendTimeline);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            InGameTimer.getInstance().tryInsertNewTimeline(copiedBuf.readString(), false);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (this.sendTimeline != null) buf.writeString(this.sendTimeline);
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewTimeline(buf.readString(), false);
    }
}
