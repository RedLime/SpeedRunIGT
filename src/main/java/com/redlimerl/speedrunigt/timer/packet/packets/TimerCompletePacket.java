package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TimerCompletePacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_co");
    private final Long sendRTA;

    public TimerCompletePacket() {
        this(null);
    }

    public TimerCompletePacket(Long rta) {
        super(IDENTIFIER);
        this.sendRTA = rta;
    }

    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.sendRTA != null) buf.writeLong(this.sendRTA);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server, byte[] bytes) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf =  new DataInputStream(buf);
            InGameTimer.complete(InGameTimer.getInstance().getStartTime() + copiedBuf.readLong(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(bytes, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendRTA != null) buf.writeLong(this.sendRTA);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.complete(InGameTimer.getInstance().getStartTime() + buf.readLong(), false);
    }
}
