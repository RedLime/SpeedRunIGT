package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public class TimerAchieveAdvancementPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ac_ad");
    private final String sendAdvancement;

    public TimerAchieveAdvancementPacket() {
        this(null);
    }

    public TimerAchieveAdvancementPacket(String advancement) {
        super(IDENTIFIER);
        this.sendAdvancement = advancement;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendAdvancement != null) {
            buf.writeString(sendAdvancement);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        TimerPacketBuf copiedBuf = buf.copy();
        InGameTimer.getInstance().tryInsertNewAdvancement(copiedBuf.readIdentifier().toString(), null, true);
        copiedBuf.release();

        int count = 0, goal = InGameTimer.getInstance().getMoreData(7441);
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }

        if (goal > 0 && count >= goal) {
            InGameTimer.complete();
            return;
        }

        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendAdvancement != null) {
            buf.writeString(sendAdvancement);
        }
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        String advancement = buf.readString();
        InGameTimer.getInstance().tryInsertNewAdvancement(advancement, null, true);
    }
}
