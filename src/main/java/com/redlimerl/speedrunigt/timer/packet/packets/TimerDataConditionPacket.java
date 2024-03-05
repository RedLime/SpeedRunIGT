package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

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
    protected void convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client) throws IOException {
        if (this.sendKey != null) buf.writeInt(this.sendKey);
        if (this.sendValue != null) buf.writeInt(this.sendValue);
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            InGameTimer.getInstance().updateMoreData(copiedBuf.readInt(), copiedBuf.readInt(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendKey != null) buf.writeInt(this.sendKey);
        if (this.sendValue != null) buf.writeInt(this.sendValue);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        InGameTimer.getInstance().updateMoreData(buf.readInt(), buf.readInt(), false);
    }
}
