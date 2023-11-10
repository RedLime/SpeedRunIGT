package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerAchieveCriteriaPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("achieve_criteria");
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
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (this.serverAdvancement != null) buf.writeString(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeString(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            InGameTimer.getInstance().tryInsertNewAdvancement(copiedBuf.readString(), copiedBuf.readString(), copiedBuf.readBoolean());
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (this.serverAdvancement != null) buf.writeString(this.serverAdvancement);
        if (this.serverCriteria != null) buf.writeString(this.serverCriteria);
        if (this.serverIsAdvancement != null) buf.writeBoolean(this.serverIsAdvancement);
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(buf.readString(), buf.readString(), buf.readBoolean());
    }
}
