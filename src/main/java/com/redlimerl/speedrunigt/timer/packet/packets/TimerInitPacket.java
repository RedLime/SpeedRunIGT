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

import java.util.Objects;

public class TimerInitPacket extends TimerPacket {

    public static final Identifier IDENTIFIER = TimerPacket.identifier("timer_init");
    private final InGameTimer sendTimer;
    private final long sendStartTime;

    public TimerInitPacket() {
        this(InGameTimer.getInstance(), InGameTimer.getInstance().getStartTime());
    }

    public TimerInitPacket(InGameTimer timer, long startTime) {
        super(IDENTIFIER);
        this.sendTimer = timer;
        this.sendStartTime = startTime;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected TimerPacketBuf convertClient2ServerPacket(TimerPacketBuf buf, MinecraftClient client) {
        if (sendTimer != null) {
            buf.writeString(sendTimer.getUuid().toString());
            buf.writeString(sendTimer.getCategory().getID());
            buf.writeLong(sendStartTime != 0 ? sendStartTime : sendTimer.getStartTime());
            buf.writeInt(sendTimer.getRunType().getCode());
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            TimerPacketBuf copiedBuf = buf.copy();
            timerInit(copiedBuf, true);
            copiedBuf.release();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected TimerPacketBuf convertServer2ClientPacket(TimerPacketBuf buf, MinecraftServer server) {
        if (sendTimer != null) {
            buf.writeString(sendTimer.getUuid().toString());
            buf.writeString(sendTimer.getCategory().getID());
            buf.writeLong(sendStartTime != 0 ? sendStartTime : sendTimer.getStartTime());
            buf.writeInt(sendTimer.getRunType().getCode());
        }
        if (!SpeedRunIGT.IS_CLIENT_SIDE && !InGameTimer.getInstance().isStarted()) {
            TimerPacketBuf copiedBuf = buf.copy();
            timerInit(copiedBuf, true);
            copiedBuf.release();
        }
        return buf;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(TimerPacketBuf buf, MinecraftClient client) {
        timerInit(buf, client.isIntegratedServerRunning());
    }

    public void timerInit(TimerPacketBuf buf, boolean isIntegrated) {
        String uuid = buf.readString();
        RunCategory category = RunCategory.getCategory(buf.readString());
        long startTime = buf.readLong();
        int runType = buf.readInt();

        if (!SpeedRunIGT.IS_CLIENT_SIDE || !Objects.equals(InGameTimer.getInstance().getUuid().toString(), uuid)) {
            InGameTimer.start("", RunType.fromInt(runType));
            InGameTimer.getInstance().setStartTime(startTime);
            InGameTimer.getInstance().setCategory(category, false);
        }
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
        InGameTimer.getInstance().setPause(false, "co-op setup");
    }
}
