package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public class TimerAchieveAdvancementPacket extends TimerPacket<TimerAchieveAdvancementPacket> {

    public static final CustomPacketPayload.Type<TimerAchieveAdvancementPacket> IDENTIFIER = TimerPacket.identifier("achieve_advancement");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerAchieveAdvancementPacket> CODEC = TimerPacket.codecOf(TimerAchieveAdvancementPacket::write, TimerAchieveAdvancementPacket::new);
    private final Identifier sendAdvancement;

    TimerAchieveAdvancementPacket(Identifier identifier) {
        super(IDENTIFIER);
        this.sendAdvancement = identifier;
    }

    public TimerAchieveAdvancementPacket(AdvancementHolder advancement) {
        this(advancement.id());
    }

    public TimerAchieveAdvancementPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readIdentifier());
    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(this.sendAdvancement);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        InGameTimer.getInstance().tryInsertNewAdvancement(this.sendAdvancement.toString(), null, true);

        int count = 0, goal = InGameTimer.getInstance().getMoreData(7441);
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }

        if (goal > 0 && count >= goal) {
            InGameTimer.complete();
            return;
        }

        this.sendPacketToPlayers(server);
    }

    @Override
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(this.sendAdvancement.toString(), null, true);
    }
}
