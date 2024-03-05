package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.sendCondition != null) buf.writeUTF(this.sendCondition.getName());
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            this.timerCondition(copiedBuf);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendCondition != null) buf.writeUTF(this.sendCondition.getName());
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        this.timerCondition(buf);
    }

    public void timerCondition(DataInputStream buf) {
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
