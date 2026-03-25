package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

public class TimerInitializePacket extends TimerPacket<TimerInitializePacket> {

    public static final CustomPacketPayload.Type<TimerInitializePacket> IDENTIFIER = TimerPacket.identifier("timer_init");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerInitializePacket> CODEC = TimerPacket.codecOf(TimerInitializePacket::write, TimerInitializePacket::new);
    private final RunType runType;
    private final RunCategory category;

    public TimerInitializePacket(InGameTimer timer) {
        super(IDENTIFIER);
        if (timer != null) {
            this.runType = timer.getRunType();
            this.category = timer.getCategory();
        } else {
            this.runType = RunType.RANDOM_SEED;
            this.category = RunCategories.ANY;
        }
    }

    public TimerInitializePacket(RegistryFriendlyByteBuf buf) {
        super(IDENTIFIER);
        this.runType = RunType.fromInt(buf.readInt());
        this.category = RunCategory.getCategory(buf.readUtf());
    }

    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.runType.getCode());
        buf.writeUtf(this.category.getID());
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            this.init(true);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(Minecraft client) {
        this.init(client.hasSingleplayerServer());
    }

    private void init(boolean isIntegrated) {
        InGameTimer.start("", this.runType);
        InGameTimer.getInstance().setStartTime(0);
        InGameTimer.getInstance().setCategory(this.category, false);
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
    }
}
