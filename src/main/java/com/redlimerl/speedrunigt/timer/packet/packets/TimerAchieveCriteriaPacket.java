package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerAchieveCriteriaPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("achieve_criteria");
    private final String serverAdvancement;
    private final String serverCriteria;
    private final Boolean isAdvancement;

    public TimerAchieveCriteriaPacket() {
        this("", "", false);
    }

    public TimerAchieveCriteriaPacket(String advancement, String criteria, Boolean isAdvancement) {
        super(IDENTIFIER);
        this.serverAdvancement = advancement;
        this.serverCriteria = criteria;
        this.isAdvancement = isAdvancement;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (serverAdvancement != null) buf.writeString(serverAdvancement);
        if (serverCriteria != null) buf.writeString(serverCriteria);
        if (isAdvancement != null) buf.writeBoolean(isAdvancement);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {

    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (serverAdvancement != null) buf.writeString(serverAdvancement);
        if (serverCriteria != null) buf.writeString(serverCriteria);
        if (isAdvancement != null) buf.writeBoolean(isAdvancement);
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        String advancement = buf.readString();
        String criteria = buf.readString();
        InGameTimer.getInstance().tryInsertNewAdvancement(advancement, criteria, isAdvancement);
    }
}
