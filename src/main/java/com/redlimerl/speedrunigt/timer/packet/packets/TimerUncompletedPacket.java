package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TimerUncompletedPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_un");

    public TimerUncompletedPacket() {
        super(IDENTIFIER);
    }

    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            InGameTimer.getInstance().setUncompleted(false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.getInstance().setUncompleted(false);
    }
}
