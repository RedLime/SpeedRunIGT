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

public class TimerCompletePacket extends TimerPacket<TimerCompletePacket> {

    public static final CustomPayload.Id<TimerCompletePacket> IDENTIFIER = TimerPacket.identifier("timer_complete");
    public static final PacketCodec<RegistryByteBuf, TimerCompletePacket> CODEC = TimerPacket.codecOf(TimerCompletePacket::write, TimerCompletePacket::new);
    private final long sendRTA;

    public TimerCompletePacket(Long rta) {
        super(IDENTIFIER);
        this.sendRTA = rta;
    }

    public TimerCompletePacket(RegistryByteBuf buf) {
        this(buf.readLong());
    }

    protected void write(RegistryByteBuf buf) {
        buf.writeLong(this.sendRTA);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.complete(InGameTimer.getInstance().getStartTime() + this.sendRTA, false);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.complete(InGameTimer.getInstance().getStartTime() + this.sendRTA, false);
    }
}
