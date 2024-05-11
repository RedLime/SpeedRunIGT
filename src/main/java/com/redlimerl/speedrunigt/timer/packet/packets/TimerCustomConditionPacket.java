package com.redlimerl.speedrunigt.timer.packet.packets;

import com.google.common.collect.Lists;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public class TimerCustomConditionPacket extends TimerPacket<TimerCustomConditionPacket> {

    public static final CustomPayload.Id<TimerCustomConditionPacket> IDENTIFIER = TimerPacket.identifier("condition_custom");
    public static final PacketCodec<RegistryByteBuf, TimerCustomConditionPacket> CODEC = TimerPacket.codecOf(TimerCustomConditionPacket::write, TimerCustomConditionPacket::new);
    private final String conditionName;

    TimerCustomConditionPacket(String conditionName) {
        super(IDENTIFIER);
        this.conditionName = conditionName;
    }

    public TimerCustomConditionPacket(CategoryCondition.Condition<?> condition) {
        this(condition.getName());
    }

    public TimerCustomConditionPacket(RegistryByteBuf buf) {
        this(buf.readString());
    }

    @Override
    protected void write(RegistryByteBuf buf) {
        buf.writeString(this.conditionName);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            this.updateTimerCondition();
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        this.updateTimerCondition();
    }

    public void updateTimerCondition() {
        for (CategoryCondition.Condition<?> condition : InGameTimer.getInstance().getCustomCondition().map(CategoryCondition::getConditionList).orElse(Lists.newArrayList())) {
            if (Objects.equals(condition.getName(), this.conditionName)) {
                condition.setCompleted(true);
                InGameTimer.getInstance().tryInsertNewTimeline(condition.getName(), false);
                break;
            }
        }
        InGameTimer.getInstance().checkConditions();
    }
}
