package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.running.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunSplitType;
import com.redlimerl.speedrunigt.timer.running.RunType;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.PacketByteBuf;

import java.util.List;

public class TimerPacketHandler {

    public static final String PACKET_TIMER_INIT_ID = SpeedRunIGT.MOD_ID + "|t_in";
    public static final String PACKET_TIMER_COMPLETE_ID = SpeedRunIGT.MOD_ID + "|t_cm";
    public static final String PACKET_TIMER_SPLIT_ID = SpeedRunIGT.MOD_ID + "|t_sp";

    private static final MinecraftClient client = MinecraftClient.getInstance();

    /*
    Timer init packets
     */
    public static void sendInitC2S(InGameTimer timer) {
        if (timer.getTimerSplit() != null)
            sendInitC2S(timer.startTime, timer.getCategory(), timer.getTimerSplit().getSeed(), timer.getTimerSplit().getRunType());
    }

    public static void sendInitC2S(long time, RunCategory category, String seed, RunType runType) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(time);
        passedData.writeString(category.getID());
        passedData.writeString(seed);
        passedData.writeString(runType.name());

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getClientConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_INIT_ID, passedData));
    }

    public static void receiveInitC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            long startTime = buffer.readLong();
            RunCategory category = RunCategory.getCategory(buffer.readString(64).trim());
            String seed = buffer.readString(64).trim();
            RunType runType = RunType.valueOf(buffer.readString(64).trim());

            sendInitS2C(server.getPlayerManager().getPlayers(), startTime, category, seed, runType);
            SpeedRunIGT.debug("server received init: " + startTime + " / " + category.getID());
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendInitS2C(List<ServerPlayerEntity> players, long startTime, RunCategory category, String seed, RunType runType) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(startTime);
        passedData.writeString(category.getID());
        passedData.writeString(seed);
        passedData.writeString(runType.name());

        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_INIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveInitS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            long startTime = buffer.readLong();
            RunCategory category = RunCategory.getCategory(buffer.readString(64).trim());
            String seed = buffer.readString(64).trim();
            RunType runType = RunType.valueOf(buffer.readString(64).trim());

            client.execute(() -> {
                if (InGameTimer.getInstance().startTime != startTime) {
                    InGameTimer.start();
                    InGameTimer.getInstance().startTime = startTime;
                    InGameTimer.getInstance().setCategory(category);
                    InGameTimer.getInstance().createNewTimerSplit(new TimerRecord(seed, runType, category));
                }
                InGameTimer.getInstance().isCoop = true;
                InGameTimer.getInstance().isServerIntegrated = MinecraftClient.getInstance().isIntegratedServerRunning();
                InGameTimer.getInstance().setPause(false);
            });

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
            client.execute(() -> InGameTimer.complete(endTime));
            SpeedRunIGT.debug("hello client complete: " + endTime);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }


    /*
    Timer split packets
     */
    public static void sendSplitC2S(RunSplitType splitType, long time) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeString(splitType.getID());
        passedData.writeLong(time);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getClientConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_SPLIT_ID, passedData));
    }

    public static void receiveSplitC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            RunSplitType splitType = RunSplitType.getSplitType(buffer.readString(64).trim());
            long time = buffer.readLong();

            sendSplitS2C(server.getPlayerManager().getPlayers(), splitType, time);
            SpeedRunIGT.debug("hello server split: " + splitType);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendSplitS2C(List<ServerPlayerEntity> players, RunSplitType splitType, long time) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeString(splitType.getID());
        passedData.writeLong(time);

        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_SPLIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveSplitS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            RunSplitType splitType = RunSplitType.getSplitType(buffer.readString(64).trim());
            long time = buffer.readLong();
            client.execute(() -> InGameTimer.getInstance().getTimerSplit().tryUpdateSplit(splitType, time, false));
            SpeedRunIGT.debug("hello client split: " + splitType);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }
}
