package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

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

    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (this.sendCondition != null) buf.writeString(sendCondition.getName());
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (this.sendCondition != null) buf.writeString(sendCondition.getName());
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        String conditionName = buf.readString();
        for (CategoryCondition.Condition<?> condition : InGameTimer.getInstance().getCustomCondition().getConditionList()) {
            if (Objects.equals(condition.getName(), conditionName)) {
                condition.setCompleted(true);
                InGameTimer.getInstance().tryInsertNewTimeline(condition.getName(), false);
                break;
            }
        }
        InGameTimer.getInstance().checkConditions();
    }
}
