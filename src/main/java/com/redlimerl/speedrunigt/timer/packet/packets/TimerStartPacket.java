package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
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

import java.util.Enumeration;
import java.util.Objects;
import java.util.UUID;

public class TimerStartPacket extends TimerPacket<TimerStartPacket> {

    public static final CustomPayload.Id<TimerStartPacket> IDENTIFIER = TimerPacket.identifier("timer_start");
    public static final PacketCodec<RegistryByteBuf, TimerStartPacket> CODEC = TimerPacket.codecOf(TimerStartPacket::write, TimerStartPacket::new);
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

    public TimerStartPacket(RegistryByteBuf buf) {
        super(IDENTIFIER);
        this.timerUuid = buf.readUuid();
        this.category = RunCategory.getCategory(buf.readString());
        this.runType = RunType.fromInt(buf.readInt());
        this.customData = buf.readString();
        this.sendRTA = buf.readLong();
    }

    protected void write(RegistryByteBuf buf) {
        buf.writeUuid(this.timerUuid);
        buf.writeString(this.category.getID());
        buf.writeInt(this.runType.getCode());
        buf.writeString(this.customData);
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
    public void receiveServer2ClientPacket(MinecraftClient client) {
        this.timerInit(client.isIntegratedServerRunning());
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
