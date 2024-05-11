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

public class TimerAchieveCriteriaPacket extends TimerPacket<TimerAchieveCriteriaPacket> {

    public static final CustomPayload.Id<TimerAchieveCriteriaPacket> IDENTIFIER = TimerPacket.identifier("achieve_criteria");
    public static final PacketCodec<RegistryByteBuf, TimerAchieveCriteriaPacket> CODEC = TimerPacket.codecOf(TimerAchieveCriteriaPacket::write, TimerAchieveCriteriaPacket::new);
    private final String serverAdvancement;
    private final String serverCriteria;
    private final Boolean serverIsAdvancement;

    public TimerAchieveCriteriaPacket(String advancement, String criteria, Boolean isAdvancement) {
        super(IDENTIFIER);
        this.serverAdvancement = advancement;
        this.serverCriteria = criteria;
        this.serverIsAdvancement = isAdvancement;
    }

    public TimerAchieveCriteriaPacket(RegistryByteBuf buf) {
        this(buf.readString(), buf.readString(), buf.readBoolean());
    }

    @Override
    protected void write(RegistryByteBuf buf) {
        buf.writeString(this.serverAdvancement);
        buf.writeString(this.serverCriteria);
        buf.writeBoolean(this.serverIsAdvancement);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.getInstance().tryInsertNewAdvancement(this.serverAdvancement, this.serverCriteria, this.serverIsAdvancement);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(this.serverAdvancement, this.serverCriteria, this.serverIsAdvancement);
    }
}
