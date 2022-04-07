package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.class_3326;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerAchieveAdvancementPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ac_ad");
    private final class_3326 sendAdvancement;

    public TimerAchieveAdvancementPacket() {
        this(null);
    }

    public TimerAchieveAdvancementPacket(class_3326 advancement) {
        super(IDENTIFIER);
        this.sendAdvancement = advancement;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendAdvancement != null) {
            buf.writeIdentifier(sendAdvancement.method_14801());
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendAdvancement != null) {
            buf.writeIdentifier(sendAdvancement.method_14801());
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        Identifier identifier = buf.readIdentifier();
        InGameTimer.getInstance().tryInsertNewAdvancement(identifier.toString(), null, true);
        InGameTimerUtils.COMPLETED_ADVANCEMENTS.add(identifier.toString());
    }
}
