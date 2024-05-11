package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Map;

public class TimerAchieveAdvancementPacket extends TimerPacket<TimerAchieveAdvancementPacket> {

    public static final CustomPayload.Id<TimerAchieveAdvancementPacket> IDENTIFIER = TimerPacket.identifier("achieve_advancement");
    public static final PacketCodec<RegistryByteBuf, TimerAchieveAdvancementPacket> CODEC = TimerPacket.codecOf(TimerAchieveAdvancementPacket::write, TimerAchieveAdvancementPacket::new);
    private final Identifier sendAdvancement;

    TimerAchieveAdvancementPacket(Identifier identifier) {
        super(IDENTIFIER);
        this.sendAdvancement = identifier;
    }

    public TimerAchieveAdvancementPacket(AdvancementEntry advancement) {
        this(advancement.id());
    }

    public TimerAchieveAdvancementPacket(RegistryByteBuf buf) {
        this(buf.readIdentifier());
    }

    @Override
    protected void write(RegistryByteBuf buf) {
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
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().tryInsertNewAdvancement(this.sendAdvancement.toString(), null, true);
    }
}
