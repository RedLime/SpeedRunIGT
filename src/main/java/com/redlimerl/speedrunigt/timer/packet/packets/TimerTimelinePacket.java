package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

public class TimerTimelinePacket extends TimerPacket<TimerTimelinePacket> {

    public static final CustomPayload.Id<TimerTimelinePacket> IDENTIFIER = TimerPacket.identifier("timer_timeline");
    public static final PacketCodec<RegistryByteBuf, TimerTimelinePacket> CODEC = TimerPacket.codecOf(TimerTimelinePacket::write, TimerTimelinePacket::new);
    private final String sendTimeline;

    public TimerTimelinePacket(String timeline) {
        super(IDENTIFIER);
        this.sendTimeline = timeline;
    }

    public TimerTimelinePacket(RegistryByteBuf buf) {
        this(buf.readString());
    }

    @Override
    protected void write(RegistryByteBuf buf) {
        buf.writeString(this.sendTimeline);
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
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewTimeline(this.sendTimeline, false);
    }
}
