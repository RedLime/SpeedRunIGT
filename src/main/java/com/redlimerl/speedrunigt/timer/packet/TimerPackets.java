package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.timer.packet.packets.*;

import static com.redlimerl.speedrunigt.timer.packet.TimerPacket.registryPacket;

public class TimerPackets {
    public static void init() {
        registryPacket(
                TimerStartPacket.IDENTIFIER, TimerStartPacket::new
        );
        registryPacket(
                TimerInitializePacket.IDENTIFIER, TimerInitializePacket::new
        );
        registryPacket(
                TimerCompletePacket.IDENTIFIER, TimerCompletePacket::new
        );
        registryPacket(
                TimerUncompletedPacket.IDENTIFIER, TimerUncompletedPacket::new
        );
        registryPacket(
                TimerChangeCategoryPacket.IDENTIFIER, TimerChangeCategoryPacket::new
        );
        registryPacket(
                TimerDataConditionPacket.IDENTIFIER, TimerDataConditionPacket::new
        );
        registryPacket(
                TimerCustomConditionPacket.IDENTIFIER, TimerCustomConditionPacket::new
        );
        registryPacket(
                TimerTimelinePacket.IDENTIFIER, TimerTimelinePacket::new
        );
        registryPacket(
                TimerAchieveAdvancementPacket.IDENTIFIER, TimerAchieveAdvancementPacket::new
        );
        registryPacket(
                TimerAchieveCriteriaPacket.IDENTIFIER, TimerAchieveCriteriaPacket::new
        );
    }
}
