package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

public class TimerInitializePacket extends TimerPacket<TimerInitializePacket> {

    public static final CustomPayload.Id<TimerInitializePacket> IDENTIFIER = TimerPacket.identifier("timer_init");
    public static final PacketCodec<RegistryByteBuf, TimerInitializePacket> CODEC = TimerPacket.codecOf(TimerInitializePacket::write, TimerInitializePacket::new);
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

    public TimerInitializePacket(RegistryByteBuf buf) {
        super(IDENTIFIER);
        this.runType = RunType.fromInt(buf.readInt());
        this.category = RunCategory.getCategory(buf.readString());
    }

    protected void write(RegistryByteBuf buf) {
        buf.writeInt(this.runType.getCode());
        buf.writeString(this.category.getID());
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
    public void receiveServer2ClientPacket(MinecraftClient client) {
        this.init(client.isIntegratedServerRunning());
    }

    private void init(boolean isIntegrated) {
        InGameTimer.start("", this.runType);
        InGameTimer.getInstance().setStartTime(0);
        InGameTimer.getInstance().setCategory(this.category, false);
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
    }
}
