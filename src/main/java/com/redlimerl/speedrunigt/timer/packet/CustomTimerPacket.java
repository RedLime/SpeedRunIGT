package com.redlimerl.speedrunigt.timer.packet;

import net.minecraft.network.packet.CustomPayload;

public class CustomTimerPacket implements CustomPayload {
    @Override
    public Id<? extends CustomPayload> getId() {
        return null;
    }
}
