package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.timer.packet.packets.*;

public class TimerPackets {
    public static void init() {
        TimerPacket.registryPacket(
                TimerStartPacket.IDENTIFIER, TimerStartPacket::new
        );
        TimerPacket.registryPacket(
                TimerInitializePacket.IDENTIFIER, TimerInitializePacket::new
        );
        TimerPacket.registryPacket(
                TimerCompletePacket.IDENTIFIER, TimerCompletePacket::new
        );
        TimerPacket.registryPacket(
                TimerUncompletedPacket.IDENTIFIER, TimerUncompletedPacket::new
        );
        TimerPacket.registryPacket(
                TimerChangeCategoryPacket.IDENTIFIER, TimerChangeCategoryPacket::new
        );
        TimerPacket.registryPacket(
                TimerDataConditionPacket.IDENTIFIER, TimerDataConditionPacket::new
        );
        TimerPacket.registryPacket(
                TimerCustomConditionPacket.IDENTIFIER, TimerCustomConditionPacket::new
        );
        TimerPacket.registryPacket(
                TimerTimelinePacket.IDENTIFIER, TimerTimelinePacket::new
        );
        TimerPacket.registryPacket(
                TimerAchieveAdvancementPacket.IDENTIFIER, TimerAchieveAdvancementPacket::new
        );
        TimerPacket.registryPacket(
                TimerAchieveCriteriaPacket.IDENTIFIER, TimerAchieveCriteriaPacket::new
        );
    }
}
