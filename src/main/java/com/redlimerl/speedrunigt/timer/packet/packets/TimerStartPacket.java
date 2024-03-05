package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.Enumeration;
import java.util.Objects;

public class TimerStartPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_sta");
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

    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client) {
        if (this.sendTimer != null) {
            buf.writeUTF(this.sendTimer.getUuid().toString());
            buf.writeUTF(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeUTF(this.customData);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server, ByteArrayInputStream bais) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            this.timerInit(copiedBuf, true);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendTimer != null) {
            buf.writeUTF(this.sendTimer.getUuid().toString());
            buf.writeUTF(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeUTF(this.customData);

            DataOutputStream copiedBuf = new DataOutputStream(buf);
            this.timerInit(copiedBuf, true);
            copiedBuf.close();
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        this.timerInit(buf, client.isIntegratedServerRunning());
    }

    public void timerInit(DataInputStream buf, boolean isIntegrated) {
        String uuid = buf.readUTF();
        RunCategory category = RunCategory.getCategory(buf.readUTF());
        long rtaTime = buf.readLong();
        int runType = buf.readInt();
        String readCustom = buf.readUTF();

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
