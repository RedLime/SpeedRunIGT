package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;

import java.io.*;

public class TimerChangeCategoryPacket extends TimerPacket {

    public static final String IDENTIFIER = TimerPacket.identifier("ti_ca");
    private final String sendCategory;

    public TimerChangeCategoryPacket() {
        this(null);
    }

    public TimerChangeCategoryPacket(String name) {
        super(IDENTIFIER);
        this.sendCategory = name;
    }

    @Override
    protected void convertClient2ServerPacket(DataOutputStream buf, MinecraftClient client) throws IOException {
        if (this.sendCategory != null) {
            buf.writeUTF(this.sendCategory);
        }
    }

    @Override
    public void receiveClient2ServerPacket(CustomPayloadC2SPacket packet, MinecraftServer server) throws IOException {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(new ByteArrayInputStream(packet.field_2455));
            RunCategory runCategory = RunCategory.getCategory(copiedBuf.readUTF());
            InGameTimer.getInstance().setCategory(runCategory, false);
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, runCategory);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(packet.field_2455, server);
    }

    @Override
    protected void convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) throws IOException {
        if (this.sendCategory != null) {
            buf.writeUTF(this.sendCategory);
        }
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) throws IOException {
        InGameTimer.getInstance().setCategory(RunCategory.getCategory(buf.readUTF()), false);
    }
}
