package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

public class TimerChangeCategoryPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_ca");
    private final String sendCategory;

    public TimerChangeCategoryPacket() {
        this(null);
    }

    public TimerChangeCategoryPacket(String name) {
        super(IDENTIFIER);
        this.sendCategory = name;
    }

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendCategory != null) {
            buf.writeString(sendCategory);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendCategory != null) {
            buf.writeString(sendCategory);
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        InGameTimer.getInstance().setCategory(RunCategory.getCategory(buf.readString()));
    }
}
