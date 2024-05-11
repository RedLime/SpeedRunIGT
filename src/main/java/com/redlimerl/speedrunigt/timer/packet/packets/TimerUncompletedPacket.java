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

public class TimerUncompletedPacket extends TimerPacket<TimerUncompletedPacket> {

    public static final CustomPayload.Id<TimerUncompletedPacket> IDENTIFIER = TimerPacket.identifier("timer_uncompleted");
    public static final PacketCodec<RegistryByteBuf, TimerUncompletedPacket> CODEC = TimerPacket.codecOf(TimerUncompletedPacket::write, buf -> new TimerUncompletedPacket());

    public TimerUncompletedPacket() {
        super(IDENTIFIER);
    }

    protected void write(RegistryByteBuf buf) {
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.getInstance().setUncompleted(false);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().setUncompleted(false);
    }
}
