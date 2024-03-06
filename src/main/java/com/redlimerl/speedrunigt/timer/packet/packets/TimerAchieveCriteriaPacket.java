package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

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

    @Environment(EnvType.CLIENT)
    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, Minecraft client) throws IOException {
        if (this.serverAdvancement != null) buf.writeUTF(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeUTF(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            InGameTimer.getInstance().tryInsertNewAdvancement(copiedBuf.readUTF(), copiedBuf.readUTF(), copiedBuf.readBoolean());
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.serverAdvancement != null) buf.writeUTF(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeUTF(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
    }


    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, Minecraft client) throws IOException {
        InGameTimer.getInstance().tryInsertNewAdvancement(buf.readUTF(), buf.readUTF(), buf.readBoolean());
    }
}
