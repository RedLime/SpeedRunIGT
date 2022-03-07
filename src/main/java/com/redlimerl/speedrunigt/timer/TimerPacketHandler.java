package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.List;

public class TimerPacketHandler {

    public static final Identifier PACKET_TIMER_INIT_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer_init");
    public static final Identifier PACKET_TIMER_COMPLETE_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer_complete");

    private static final MinecraftClient client = MinecraftClient.getInstance();

    /*
    Timer init packets
     */
    public static void sendInitC2S(InGameTimer timer) {
        sendInitC2S(timer.startTime, timer.getCategory(), timer.getSeedName(), timer.isSetSeed());
    }

    public static void sendInitC2S(long time, RunCategory category, String seedName, boolean isSetSeed) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(time);
        passedData.writeString(category.getID());
        passedData.writeString(seedName);
        passedData.writeBoolean(isSetSeed);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getClientConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_INIT_ID, passedData));
    }

    public static void receiveInitC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            long startTime = buffer.readLong();
            RunCategory category = RunCategory.getCategory(buffer.readString(128).trim());
            String seedName = buffer.readString(256).trim();
            boolean isSetSeed = buffer.readBoolean();

            sendInitS2C(server.getPlayerManager().getPlayers(), startTime, category, seedName, isSetSeed);
            SpeedRunIGT.debug("server received init: " + startTime + " / " + category.getID());
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendInitS2C(List<ServerPlayerEntity> players, long startTime, RunCategory category, String seedName, boolean isSetSeed) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(startTime);
        passedData.writeString(category.getID());
        passedData.writeString(seedName);
        passedData.writeBoolean(isSetSeed);

        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_INIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveInitS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            long startTime = buffer.readLong();
            RunCategory category = RunCategory.getCategory(buffer.readString(128).trim());
            String seedName = buffer.readString(256).trim();
            boolean isSetSeed = buffer.readBoolean();

            if (InGameTimer.getInstance().startTime != startTime) {
                InGameTimer.start("", seedName, isSetSeed);
                InGameTimer.getInstance().startTime = startTime;
                InGameTimer.getInstance().setCategory(category);
            }
            InGameTimer.getInstance().isCoop = true;
            InGameTimer.getInstance().isServerIntegrated = MinecraftClient.getInstance().isIntegratedServerRunning();
            InGameTimer.getInstance().setPause(false, "co-op setup");

            SpeedRunIGT.debug("client received init: " + startTime + " / " + category.getID());
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }


    /*
    Timer complete packets
     */
    public static void sendCompleteC2S(InGameTimer timer) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(timer.endTime);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getClientConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_COMPLETE_ID, passedData));
    }

    public static void receiveCompleteC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            long endTime = buffer.readLong();

            sendCompleteS2C(server.getPlayerManager().getPlayers(), endTime);
            SpeedRunIGT.debug("hello server complete: " + endTime);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendCompleteS2C(List<ServerPlayerEntity> players, long endTime) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(endTime);
        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_COMPLETE_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveCompleteS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            long endTime = buffer.readLong();
            InGameTimer.complete(endTime);
            SpeedRunIGT.debug("hello client complete: " + endTime);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }
}
