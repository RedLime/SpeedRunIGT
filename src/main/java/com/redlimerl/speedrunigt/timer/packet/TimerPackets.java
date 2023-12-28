package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.timer.packet.packets.*;

import static com.redlimerl.speedrunigt.timer.packet.TimerPacket.registryPacket;

public class TimerPackets {
    public static void init(TimerPacket.Side side) {
        registryPacket(
                TimerStartPacket.IDENTIFIER, TimerStartPacket::new, side
        );
        registryPacket(
                TimerInitializePacket.IDENTIFIER, TimerInitializePacket::new, side
        );
        registryPacket(
                TimerCompletePacket.IDENTIFIER, TimerCompletePacket::new, side
        );
        registryPacket(
                TimerUncompletedPacket.IDENTIFIER, TimerUncompletedPacket::new, side
        );
        registryPacket(
                TimerChangeCategoryPacket.IDENTIFIER, TimerChangeCategoryPacket::new, side
        );
        registryPacket(
                TimerDataConditionPacket.IDENTIFIER, TimerDataConditionPacket::new, side
        );
        registryPacket(
                TimerCustomConditionPacket.IDENTIFIER, TimerCustomConditionPacket::new, side
        );
        registryPacket(
                TimerTimelinePacket.IDENTIFIER, TimerTimelinePacket::new, side
        );
        registryPacket(
                TimerAchieveAdvancementPacket.IDENTIFIER, TimerAchieveAdvancementPacket::new, side
        );
        registryPacket(
                TimerAchieveCriteriaPacket.IDENTIFIER, TimerAchieveCriteriaPacket::new, side
        );
    }
}
