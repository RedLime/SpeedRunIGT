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
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

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

    @Environment(EnvType.CLIENT)
    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client) throws IOException {
        buf.writeInt(this.runType.getCode());
        buf.writeUTF(this.category.getID());
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            this.init(copiedBuf, true);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        buf.writeInt(this.runType.getCode());
        buf.writeUTF(this.category.getID());

        DataOutputStream copiedBuf = new DataOutputStream(buf);
        this.init(this.runType.getCode(), RunCategory.getCategory(this.category.getID()), true);
        copiedBuf.close();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        this.init(buf, client.isIntegratedServerRunning());
    }

    private void init(DataInputStream buf, boolean isIntegrated) throws IOException {
        int runType = buf.readInt();
        RunCategory category = RunCategory.getCategory(buf.readUTF());
        init(runType, category, isIntegrated);
    }

    private void init(int runType, RunCategory category, boolean isIntegrated) {
        InGameTimer.start("", RunType.fromInt(runType));
        InGameTimer.getInstance().setStartTime(0);
        InGameTimer.getInstance().setCategory(category, false);
        InGameTimer.getInstance().setCoop(true);
        InGameTimer.getInstance().setServerIntegrated(isIntegrated);
    }
}
