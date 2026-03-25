package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public class TimerTimelinePacket extends TimerPacket<TimerTimelinePacket> {

    public static final CustomPacketPayload.Type<TimerTimelinePacket> IDENTIFIER = TimerPacket.identifier("timer_timeline");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerTimelinePacket> CODEC = TimerPacket.codecOf(TimerTimelinePacket::write, TimerTimelinePacket::new);
    private final String sendTimeline;

    public TimerTimelinePacket(String timeline) {
        super(IDENTIFIER);
        this.sendTimeline = timeline;
    }

    public TimerTimelinePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.sendTimeline);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.getInstance().tryInsertNewTimeline(this.sendTimeline, false);
        }
        this.sendPacketToPlayers( server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().tryInsertNewTimeline(this.sendTimeline, false);
    }
}
