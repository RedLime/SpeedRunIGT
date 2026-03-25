package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
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

import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

public class TimerStartPacket extends TimerPacket<TimerStartPacket> {

    public static final CustomPacketPayload.Type<TimerStartPacket> IDENTIFIER = TimerPacket.identifier("timer_start");
    public static final StreamCodec<RegistryFriendlyByteBuf, TimerStartPacket> CODEC = TimerPacket.codecOf(TimerStartPacket::write, TimerStartPacket::new);
    private final UUID timerUuid;
    private final RunType runType;
    private final RunCategory category;
    private final String customData;
    private final long sendRTA;

    public TimerStartPacket(InGameTimer timer, long rta) {
        super(IDENTIFIER);
        StringBuilder stringBuilder = new StringBuilder();
        Enumeration<Integer> keyInt = timer.getMoreDataKeys();
        while (keyInt.hasMoreElements()) {
            Integer key = keyInt.nextElement();
            Integer value = timer.getMoreData(key);
            stringBuilder.append(key).append(",").append(value).append(";");
        }
        this.timerUuid = timer.getUuid();
        this.category = timer.getCategory();
        this.runType = timer.getRunType();
        this.customData = stringBuilder.substring(0, stringBuilder.length() - (stringBuilder.length() > 0 ? 1 : 0));
        this.sendRTA = rta;
    }

    public TimerStartPacket(RegistryFriendlyByteBuf buf) {
        super(IDENTIFIER);
        this.timerUuid = buf.readUUID();
        this.category = RunCategory.getCategory(buf.readUtf());
        this.runType = RunType.fromInt(buf.readInt());
        this.customData = buf.readUtf();
        this.sendRTA = buf.readLong();
    }

    protected void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.timerUuid);
        buf.writeUtf(this.category.getID());
        buf.writeInt(this.runType.getCode());
        buf.writeUtf(this.customData);
        buf.writeLong(this.sendRTA);
    }

    @Override
    public void receiveClient2ServerPacket(MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            this.timerInit(true);
        }
        this.sendPacketToPlayers(server);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(Minecraft client) {
        this.timerInit(client.hasSingleplayerServer());
    }

    public void timerInit(boolean isIntegrated) {
        long startTime = System.currentTimeMillis() - this.sendRTA;

        if (!SpeedRunIGT.IS_CLIENT_SIDE || !Objects.equals(InGameTimer.getInstance().getUuid().toString(), this.timerUuid.toString())) {
            InGameTimer.start("", this.runType);
            InGameTimer.getInstance().setStartTime(startTime);
            InGameTimer.getInstance().setCategory(category, false);
        }
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);

        if (!this.customData.isEmpty()) {
            for (String customString : this.customData.split(";")) {
                String[] data = customString.split(",");
                int key = Integer.parseInt(data[0]);
                int value = Integer.parseInt(data[1]);
                InGameTimer.getInstance().updateMoreData(key, value);
            }
        }
    }
}
