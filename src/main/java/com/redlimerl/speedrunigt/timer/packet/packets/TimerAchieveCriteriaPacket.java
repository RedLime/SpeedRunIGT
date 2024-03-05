package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TimerAchieveCriteriaPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ac_cr");
    private final String serverAdvancement;
    private final String serverCriteria;
    private final Boolean serverIsAdvancement;

    public TimerAchieveCriteriaPacket() {
        this(null, null, null);
    }

    public TimerAchieveCriteriaPacket(String advancement, String criteria, Boolean isAdvancement) {
        super(IDENTIFIER);
        this.serverAdvancement = advancement;
        this.serverCriteria = criteria;
        this.serverIsAdvancement = isAdvancement;
    }

    @Override
    protected DataOutputStream convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.serverAdvancement != null) buf.writeUTF(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeUTF(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            InGameTimer.getInstance().tryInsertNewAdvancement(copiedBuf.readUTF(), copiedBuf.readUTF(), copiedBuf.readBoolean());
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.serverAdvancement != null) buf.writeUTF(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeUTF(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
        return buf;
    }

    @(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(buf.readUTF(), buf.readUTF(), buf.readBoolean());
    }
}
