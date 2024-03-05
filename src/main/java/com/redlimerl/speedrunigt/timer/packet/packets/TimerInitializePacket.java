package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class TimerInitializePacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_in");
    private final RunType runType;
    private final RunCategory category;

    public TimerInitializePacket() {
        this(null);
    }

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

    @Override
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        buf.writeInt(this.runType.getCode());
        buf.writeUTF(this.category.getID());
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            this.init(copiedBuf, true);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        buf.writeInt(this.runType.getCode());
        buf.writeUTF(this.category.getID());

        DataOutputStream copiedBuf = new DataOutputStream(buf);
        this.init(copiedBuf, true);
        copiedBuf.close();
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        this.init(buf, client.isIntegratedServerRunning());
    }

    private void init(DataInputStream buf, boolean isIntegrated) {
        int runType = buf.readInt();
        RunCategory category = RunCategory.getCategory(buf.readUTF());

        InGameTimer.start("", RunType.fromInt(runType));
        InGameTimer.getInstance().setStartTime(0);
        InGameTimer.getInstance().setCategory(category, false);
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
    }
}
