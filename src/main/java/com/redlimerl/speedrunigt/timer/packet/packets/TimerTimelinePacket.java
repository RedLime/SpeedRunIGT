package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.sendTimeline != null) buf.writeUTF(this.sendTimeline);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            InGameTimer.getInstance().tryInsertNewTimeline(copiedBuf.readUTF(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendTimeline != null) buf.writeUTF(this.sendTimeline);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewTimeline(buf.readUTF(), false);
    }
}
