package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

public class TimerChangeCategoryPacket extends TimerPacket<TimerChangeCategoryPacket> {

    public static final CustomPayload.Id<TimerChangeCategoryPacket> IDENTIFIER = TimerPacket.identifier("timer_category");
    public static final PacketCodec<RegistryByteBuf, TimerChangeCategoryPacket> CODEC = TimerPacket.codecOf(TimerChangeCategoryPacket::write, TimerChangeCategoryPacket::new);
    private final RunCategory category;

    public TimerChangeCategoryPacket(RunCategory category) {
        super(IDENTIFIER);
        this.category = category;
    }

    public TimerChangeCategoryPacket(RegistryByteBuf buf) {
        this(RunCategory.getCategory(buf.readString()));
    }

    @Override
    protected void write(RegistryByteBuf buf) {
        buf.writeString(this.category.getID());
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimer.getInstance().setCategory(this.category, false);
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, this.category);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(MinecraftClient client) {
        InGameTimer.getInstance().setCategory(this.category, false);
    }
}
