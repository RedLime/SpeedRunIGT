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

public class TimerAchieveCriteriaPacket extends TimerPacket<TimerAchieveCriteriaPacket> {

    public static final CustomPacketPayload.Type<TimerAchieveCriteriaPacket> IDENTIFIER = TimerPacket.identifier("achieve_criteria");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerAchieveCriteriaPacket> CODEC = TimerPacket.codecOf(TimerAchieveCriteriaPacket::write, TimerAchieveCriteriaPacket::new);
    private final String serverAdvancement;
    private final String serverCriteria;
    private final Boolean serverIsAdvancement;

    public TimerAchieveCriteriaPacket(String advancement, String criteria, Boolean isAdvancement) {
        super(IDENTIFIER);
        this.serverAdvancement = advancement;
        this.serverCriteria = criteria;
        this.serverIsAdvancement = isAdvancement;
    }

    public TimerAchieveCriteriaPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.serverAdvancement);
        buf.writeUtf(this.serverCriteria);
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
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(this.serverAdvancement, this.serverCriteria, this.serverIsAdvancement);
    }
}
