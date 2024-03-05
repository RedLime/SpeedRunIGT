package com.redlimerl.speedrunigt.timer.packet.packets;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.packet.TimerPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
    protected DataOutputStream createC2SPacket(DataOutputStream buf, MinecraftClient client, ByteArrayOutputStream baos) {
        if (this.sendCategory != null) {
            buf.writeUTF(this.sendCategory);
        }
        return buf;
    }

    @Override
    public void receiveClient2ServerPacket(DataInputStream buf, MinecraftServer server) {
        if (!SpeedRunIGT.IS_CLIENT_SIDE) {
            DataInputStream copiedBuf = new DataInputStream(buf);
            RunCategory runCategory = RunCategory.getCategory(copiedBuf.readUTF());
            InGameTimer.getInstance().setCategory(runCategory, false);
            SpeedRunOption.setOption(SpeedRunOptions.TIMER_CATEGORY, runCategory);
            copiedBuf.close();
        }
        this.sendPacketToPlayers(buf, server);
    }

    @Override
    protected DataOutputStream convertServer2ClientPacket(DataOutputStream buf, MinecraftServer server) {
        if (this.sendCategory != null) {
            buf.writeUTF(this.sendCategory);
        }
        return buf;
    }

    @Override
    public void receiveServer2ClientPacket(DataInputStream buf, MinecraftClient client) {
        InGameTimer.getInstance().setCategory(RunCategory.getCategory(buf.readUTF()), false);
    }
}
