package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Maps;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.SimpleAdvancement;
import net.minecraft.class_3258;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class TimerAchieveAdvancementPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("achieve_advancement");
    private final SimpleAdvancement sendAdvancement;

    public TimerAchieveAdvancementPacket() {
        this(null);
    }

    public TimerAchieveAdvancementPacket(SimpleAdvancement advancement) {
        super(IDENTIFIER);
        this.sendAdvancement = advancement;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendAdvancement != null) {
            buf.writeIdentifier(sendAdvancement.getIdentifier());
            sendAdvancement.asTaskAdvancement().writeToByteBuf(buf.getBuffer());
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
            buf.writeIdentifier(sendAdvancement.getIdentifier());
            sendAdvancement.asTaskAdvancement().writeToByteBuf(buf.getBuffer());
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        Identifier identifier = buf.readIdentifier();
        if (buf.readBoolean()) buf.readIdentifier(); // Get Parent ID
        AdvancementDisplay advancementDisplay = buf.readBoolean() ? AdvancementDisplay.fromPacketByteBuf(buf.getBuffer()) : null;
        if (advancementDisplay != null && identifier != null) {
            SimpleAdvancement advancement = new SimpleAdvancement(identifier, null, advancementDisplay, null, Maps.newHashMap(), null);

            if (advancementDisplay.method_15014() && !InGameTimerUtils.COMPLETED_ADVANCEMENTS.contains(identifier.toString())) {
                MinecraftClient.getInstance().method_14462().method_14491(new class_3258(advancement));
            }

            InGameTimer.getInstance().tryInsertNewAdvancement(identifier.toString(), null, true);
            InGameTimerUtils.COMPLETED_ADVANCEMENTS.add(identifier.toString());
        }
    }
}
