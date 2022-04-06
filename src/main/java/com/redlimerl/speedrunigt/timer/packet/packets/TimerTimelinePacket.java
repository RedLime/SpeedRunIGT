package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
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

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendTimeline != null) buf.writeString(sendTimeline);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendTimeline != null) buf.writeString(sendTimeline);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewTimeline(buf.readString(), false);
    }
}
