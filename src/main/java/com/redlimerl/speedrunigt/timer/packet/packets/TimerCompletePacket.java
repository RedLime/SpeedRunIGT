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

public class TimerCompletePacket extends TimerPacket<TimerCompletePacket> {

    public static final CustomPacketPayload.Type<TimerCompletePacket> IDENTIFIER = TimerPacket.identifier("timer_complete");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerCompletePacket> CODEC = TimerPacket.codecOf(TimerCompletePacket::write, TimerCompletePacket::new);
    private final long sendRTA;

    public TimerCompletePacket(Long rta) {
        super(IDENTIFIER);
        this.sendRTA = rta;
    }

    public TimerCompletePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readLong());
    }

    protected void write(RegistryFriendlyByteBuf buf) {
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
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.complete(InGameTimer.getInstance().getStartTime() + this.sendRTA, false);
    }
}
