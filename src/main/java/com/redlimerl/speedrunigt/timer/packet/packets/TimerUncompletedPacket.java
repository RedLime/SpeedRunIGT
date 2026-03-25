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

public class TimerUncompletedPacket extends TimerPacket<TimerUncompletedPacket> {

    public static final CustomPacketPayload.Type<TimerUncompletedPacket> IDENTIFIER = TimerPacket.identifier("timer_uncompleted");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerUncompletedPacket> CODEC = TimerPacket.codecOf(TimerUncompletedPacket::write, buf -> new TimerUncompletedPacket());

    public TimerUncompletedPacket() {
        super(IDENTIFIER);
    }

    protected void write(RegistryFriendlyByteBuf buf) {
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
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().setUncompleted(false);
    }
}
