package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public class TimerChangeCategoryPacket extends TimerPacket<TimerChangeCategoryPacket> {

    public static final CustomPacketPayload.Type<TimerChangeCategoryPacket> IDENTIFIER = TimerPacket.identifier("timer_category");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerChangeCategoryPacket> CODEC = TimerPacket.codecOf(TimerChangeCategoryPacket::write, TimerChangeCategoryPacket::new);
    private final RunCategory category;

    public TimerChangeCategoryPacket(RunCategory category) {
        super(IDENTIFIER);
        this.category = category;
    }

    public TimerChangeCategoryPacket(RegistryFriendlyByteBuf buf) {
        this(RunCategory.getCategory(buf.readUtf()));
    }

    @Override
    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.category.getID());
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
    public void receiveServer2ClientPacket(Minecraft client) {
        InGameTimer.getInstance().setCategory(this.category, false);
    }
}
