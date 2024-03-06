package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.Objects;

public class TimerCustomConditionPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("co_cu");
    private final CategoryCondition.Condition<?> sendCondition;

    public TimerCustomConditionPacket() {
        this(null);
    }

    public TimerCustomConditionPacket(CategoryCondition.Condition<?> condition) {
        super(IDENTIFIER);
        this.sendCondition = condition;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, Minecraft client) throws IOException {
        if (this.sendCondition != null) buf.writeUTF(this.sendCondition.getName());
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            this.timerCondition(copiedBuf);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendCondition != null) buf.writeUTF(this.sendCondition.getName());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, Minecraft client) throws IOException {
        this.timerCondition(buf);
    }

    public void timerCondition(DataInputStream buf) throws IOException {
        String conditionName = buf.readUTF();
        for (CategoryCondition.Condition<?> condition : InGameTimer.getInstance().getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
            if (Objects.equals(condition.getName(), conditionName)) {
                condition.setCompleted(true);
                InGameTimer.getInstance().tryInsertNewTimeline(condition.getName(), false);
                break;
            }
        }
        InGameTimer.getInstance().checkConditions();
    }
}
