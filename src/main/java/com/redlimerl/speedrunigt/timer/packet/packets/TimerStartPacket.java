package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.packet.TimerPacketBuf;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Enumeration;
import java.util.Objects;

public class TimerStartPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("timer_start");
    private final InGameTimer sendTimer;
    private final String customData;
    private final long sendRTA;

    public TimerStartPacket() {
        this(null, InGameTimer.getInstance().getRealTimeAttack());
    }

    public TimerStartPacket(InGameTimer timer, long rta) {
        super(IDENTIFIER);
        this.sendTimer = timer;
        StringBuilder stringBuilder = new StringBuilder();
        if (timer != null) {
            Enumeration<Integer> keyInt = timer.getMoreDataKeys();
            while (keyInt.hasMoreElements()) {
                Integer key = keyInt.nextElement();
                Integer value = timer.getMoreData(key);
                stringBuilder.append(key).append(",").append(value).append(";");
            }
        }
        this.customData = stringBuilder.substring(0, stringBuilder.length() - (stringBuilder.length() > 0 ? 1 : 0));
        this.sendRTA = rta;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (this.sendTimer != null) {
            buf.writeString(this.sendTimer.getUuid().toString());
            buf.writeString(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeString(this.customData);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            this.timerInit(copiedBuf, true);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (this.sendTimer != null) {
            buf.writeString(this.sendTimer.getUuid().toString());
            buf.writeString(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeString(this.customData);

            TimerPacketBuf copiedBuf = buf.copy();
            this.timerInit(copiedBuf, true);
            copiedBuf.release();
        }
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        this.timerInit(buf, client.isIntegratedServerRunning());
    }

    public void timerInit(TimerPacketBuf buf, boolean isIntegrated) {
        String uuid = buf.readString();
        RunCategory category = RunCategory.getCategory(buf.readString());
        long rtaTime = buf.readLong();
        int runType = buf.readInt();
        String readCustom = buf.readString();

        long startTime = System.currentTimeMillis() - rtaTime;

        if (!SpeedRunIGT.IS_CLIENT_SIDE || !Objects.equals(InGameTimer.getInstance().getUuid().toString(), uuid)) {
            InGameTimer.start("", RunType.fromInt(runType));
            InGameTimer.getInstance().setStartTime(startTime);
            InGameTimer.getInstance().setCategory(category, false);
        }
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);

        if (!readCustom.isEmpty()) {
            for (String customString : readCustom.split(";")) {
                String[] data = customString.split(",");
                int key = Integer.parseInt(data[0]);
                int value = Integer.parseInt(data[1]);
                InGameTimer.getInstance().updateMoreData(key, value);
            }
        }
    }
}
