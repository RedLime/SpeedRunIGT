package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;

public class TimerInitializePacket extends TimerPacket {

    public static final CustomPayload.Id<CustomPayload> IDENTIFIER = TimerPacket.identifier("timer_init");
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

    public void write(RegistryByteBuf buf) {
        buf.writeInt(this.runType.getCode());
        buf.writeString(this.category.getID());
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        buf.writeInt(this.runType.getCode());
        buf.writeString(this.category.getID());
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            this.init(copiedBuf, true);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        buf.writeInt(this.runType.getCode());
        buf.writeString(this.category.getID());

        TimerPacketBuf copiedBuf = buf.copy();
        this.init(copiedBuf, true);
        copiedBuf.release();
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        this.init(buf, client.isIntegratedServerRunning());
    }

    private TimerInitializePacket init(TimerPacketBuf buf, boolean isIntegrated) {
        int runType = buf.readInt();
        RunCategory category = RunCategory.getCategory(buf.readString());

        InGameTimer.start("", RunType.fromInt(runType));
        InGameTimer.getInstance().setStartTime(0);
        InGameTimer.getInstance().setCategory(category, false);
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
        return this;
    }
}
