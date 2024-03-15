package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
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

    @Environment(EnvType.CLIENT)
    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client) throws IOException {
        if (this.sendTimer != null) {
            buf.writeUTF(this.sendTimer.getUuid().toString());
            buf.writeUTF(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeUTF(this.customData);
        }
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            this.timerInit(copiedBuf, true);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendTimer != null) {
            buf.writeUTF(this.sendTimer.getUuid().toString());
            buf.writeUTF(this.sendTimer.getCategory().getID());
            buf.writeLong(this.sendRTA);
            buf.writeInt(this.sendTimer.getRunType().getCode());
            buf.writeUTF(this.customData);

            DataOutputStream copiedBuf = new DataOutputStream(buf);
            this.timerInit(
                    this.sendTimer.getUuid().toString(),
                    this.sendTimer.getCategory(),
                    this.sendRTA,
                    this.sendTimer.getRunType().getCode(),
                    this.customData,
                    true
            );
            copiedBuf.close();
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        this.timerInit(buf, client.isIntegratedServerRunning());
    }

    public void timerInit(DataInputStream buf, boolean isIntegrated) throws IOException {
        String uuid = buf.readUTF();
        RunCategory category = RunCategory.getCategory(buf.readUTF());
        long rtaTime = buf.readLong();
        int runType = buf.readInt();
        String readCustom = buf.readUTF();

        timerInit(uuid, category, rtaTime, runType, readCustom, isIntegrated);
    }

    public void timerInit(String uuid, RunCategory category, long rtaTime, int runType, String readCustom, boolean isIntegrated) {
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
