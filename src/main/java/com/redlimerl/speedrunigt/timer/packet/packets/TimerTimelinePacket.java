package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

public class TimerTimelinePacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_ti");
    private final String sendTimeline;

    public TimerTimelinePacket() {
        this("");
    }

    public TimerTimelinePacket(String timeline) {
        super(IDENTIFIER);
        this.sendTimeline = timeline;
    }

    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client) throws IOException {
        if (this.sendTimeline != null) buf.writeUTF(this.sendTimeline);
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            InGameTimer.getInstance().tryInsertNewTimeline(copiedBuf.readUTF(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendTimeline != null) buf.writeUTF(this.sendTimeline);
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        InGameTimer.getInstance().tryInsertNewTimeline(buf.readUTF(), false);
    }
}
