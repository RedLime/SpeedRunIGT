package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerAchieveAdvancementPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("achieve_advancement");
    private final Advancement sendAdvancement;

    public TimerAchieveAdvancementPacket() {
        this(null);
    }

    public TimerAchieveAdvancementPacket(Advancement advancement) {
        super(IDENTIFIER);
        this.sendAdvancement = advancement;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendAdvancement != null) {
            buf.writeIdentifier(sendAdvancement.getId());
            sendAdvancement.createTask().toPacket(buf.getBuffer());
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
            buf.writeIdentifier(sendAdvancement.getId());
            sendAdvancement.createTask().toPacket(buf.getBuffer());
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        Identifier identifier = buf.readIdentifier();
        if (buf.readBoolean()) buf.readIdentifier(); // Get Parent ID
        AdvancementDisplay advancementDisplay = buf.readBoolean() ? AdvancementDisplay.fromPacket(buf.getBuffer()) : null;
        if (advancementDisplay != null && identifier != null) {
            Advancement advancement = new Advancement(identifier, null, advancementDisplay, null, Maps.newHashMap(), null);

            if (advancementDisplay.shouldShowToast() && !InGameTimerUtils.COMPLETED_ADVANCEMENTS.contains(identifier.toString())) {
                MinecraftClient.getInstance().getToastManager().add(new AdvancementToast(advancement));
            }

            InGameTimer.getInstance().tryInsertNewAdvancement(identifier.toString(), null, true);
            InGameTimerUtils.COMPLETED_ADVANCEMENTS.add(identifier.toString());
        }
    }
}
