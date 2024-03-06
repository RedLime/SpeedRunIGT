package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

public class TimerCompletePacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_co");
    private final Long sendRTA;

    public TimerCompletePacket() {
        this(null);
    }

    public TimerCompletePacket(Long rta) {
        super(IDENTIFIER);
        this.sendRTA = rta;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, Minecraft client) throws IOException {
        if (this.sendRTA != null) buf.writeLong(this.sendRTA);
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf =  new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            InGameTimer.complete(InGameTimer.getInstance().getStartTime() + copiedBuf.readLong(), false);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendRTA != null) buf.writeLong(this.sendRTA);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, Minecraft client) throws IOException {
        InGameTimer.complete(InGameTimer.getInstance().getStartTime() + buf.readLong(), false);
    }
}
