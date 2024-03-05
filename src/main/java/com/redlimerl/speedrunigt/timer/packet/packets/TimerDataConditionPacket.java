package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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

    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.sendKey != null) buf.writeInt(this.sendKey);
        if (this.sendValue != null) buf.writeInt(this.sendValue);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            // TODO: is this a valid way to copy?
            DataInputStream copiedBuf = new DataInputStream(buf);
            InGameTimer.getInstance().updateMoreData(copiedBuf.readInt(), copiedBuf.readInt(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendKey != null) buf.writeInt(this.sendKey);
        if (this.sendValue != null) buf.writeInt(this.sendValue);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.getInstance().updateMoreData(buf.readInt(), buf.readInt(), false);
    }
}
