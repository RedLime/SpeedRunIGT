package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public class TimerAchieveCriteriaPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ac_cr");
    private final String serverAdvancement;
    private final String serverCriteria;
    private final Boolean serverIsAdvancement;

    public TimerAchieveCriteriaPacket() {
        this("", "", false);
    }

    public TimerAchieveCriteriaPacket(String advancement, String criteria, Boolean isAdvancement) {
        super(IDENTIFIER);
        this.serverAdvancement = advancement;
        this.serverCriteria = criteria;
        this.serverIsAdvancement = isAdvancement;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (serverAdvancement != null) buf.writeString(serverAdvancement);
        if (serverCriteria != null) buf.writeString(serverCriteria);
        if (serverIsAdvancement != null) buf.writeBoolean(serverIsAdvancement);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {

    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (serverAdvancement != null) buf.writeString(serverAdvancement);
        if (serverCriteria != null) buf.writeString(serverCriteria);
        if (serverIsAdvancement != null) buf.writeBoolean(serverIsAdvancement);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        String advancement = buf.readString();
        String criteria = buf.readString();
        boolean isAdvancement = buf.readBoolean();
        InGameTimer.getInstance().tryInsertNewAdvancement(advancement, criteria, isAdvancement);
    }
}
