package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class TimerCustomConditionPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("condition_custom");
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
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (this.sendCondition != null) buf.writeString(this.sendCondition.getName());
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            this.timerCondition(copiedBuf);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (this.sendCondition != null) buf.writeString(this.sendCondition.getName());
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        this.timerCondition(buf);
    }

    public void timerCondition(TimerPacketBuf buf) {
        String conditionName = buf.readString();
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
