package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.*;
import java.util.Map;

public class TimerAchieveAdvancementPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ac_ad");
    private final String sendAdvancement;
ByteArrayOutputStream
    public TimerAchieveAdvancementPacket() {
        this(null);
    }

    public TimerAchieveAdvancementPacket(String advancement) {
        super(IDENTIFIER);
        this.sendAdvancement = advancement;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (sendAdvancement != null) {
            buf.writeUTF(sendAdvancement);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server, byte[] bytes) {
        DataInputStream copiedBuf = new DataInputStream(buf);
        InGameTimer.getInstance().tryInsertNewAdvancement(new Identifier(copiedBuf.readUTF()), null, true);
        copiedBuf.close();

        int count = 0, goal = InGameTimer.getInstance().getMoreData(7441);
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }

        if (goal > 0 && count >= goal) {
            InGameTimer.complete();
            return;
        }

        this.sendPacketToPlayers(bytes, server);
    }

    @Override
    protected DataOutputStream createS2CPacket(DataOutputStream buf, MinecraftServer server) {
        if (sendAdvancement != null) {
            buf.writeUTF(sendAdvancement);
        }
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        String advancement = buf.readUTF();
        InGameTimer.getInstance().tryInsertNewAdvancement(advancement, null, true);
    }
}
