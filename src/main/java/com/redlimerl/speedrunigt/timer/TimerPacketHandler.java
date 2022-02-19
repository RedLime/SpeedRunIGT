package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.TimerSplit.SplitType;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class TimerPacketHandler {

    public static final Identifier PACKET_TIMER_INIT_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer_init");
    public static final Identifier PACKET_TIMER_COMPLETE_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer_complete");
    public static final Identifier PACKET_TIMER_SPLIT_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer_split");

    private static final MinecraftClient client = MinecraftClient.getInstance();

    /*
    Timer init packets
     */
    public static void sendInitC2S(InGameTimer timer) {
        if (timer.getTimerSplit() != null)
            sendInitC2S(timer.startTime, timer.getCategory(), timer.getTimerSplit().getSeed(), timer.getTimerSplit().getRunType());
    }

    public static void sendInitC2S(long time, RunCategory category, String seed, RunType runType) {
        if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(time);
        passedData.writeEnumConstant(category);
        passedData.writeString(seed);
        passedData.writeEnumConstant(runType);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_INIT_ID, passedData));
    }

    public static void receiveInitC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            long startTime = buffer.readLong();
            RunCategory category = buffer.readEnumConstant(RunCategory.class);
            String seed = buffer.readString();
            RunType runType = buffer.readEnumConstant(RunType.class);

            sendInitS2C(server.getPlayerManager().getPlayerList(), startTime, category, seed, runType);
            SpeedRunIGT.debug("server received init: " + startTime + " / " + category.name());
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendInitS2C(List<ServerPlayerEntity> players, long startTime, RunCategory category, String seed, RunType runType) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(startTime);
        passedData.writeEnumConstant(category);
        passedData.writeString(seed);
        passedData.writeEnumConstant(runType);

        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_INIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveInitS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            long startTime = buffer.readLong();
            RunCategory category = buffer.readEnumConstant(RunCategory.class);
            String seed = buffer.readString();
            RunType runType = buffer.readEnumConstant(RunType.class);

            client.execute(() -> {
                if (InGameTimer.getInstance().startTime != startTime) {
                    InGameTimer.start();
                    InGameTimer.getInstance().startTime = startTime;
                    InGameTimer.getInstance().setCategory(category);
                    InGameTimer.getInstance().createNewTimerSplit(new TimerSplit(seed, runType, category));
                }
                InGameTimer.getInstance().isCoop = true;
                InGameTimer.getInstance().isServerIntegrated = MinecraftClient.getInstance().isIntegratedServerRunning();
                InGameTimer.getInstance().setPause(false);
            });

            SpeedRunIGT.debug("client received init: " + startTime + " / " + category.name());
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }


    /*
    Timer complete packets
     */
    public static void sendCompleteC2S(InGameTimer timer) {
        if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(timer.endTime);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_COMPLETE_ID, passedData));
    }

    public static void receiveCompleteC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            long endTime = buffer.readLong();

            sendCompleteS2C(server.getPlayerManager().getPlayerList(), endTime);
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
            if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
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
    public static void sendSplitC2S(SplitType splitType, long time) {
        if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeEnumConstant(splitType);
        passedData.writeLong(time);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_SPLIT_ID, passedData));
    }

    public static void receiveSplitC2S(MinecraftServer server, PacketByteBuf buffer) {
        try {
            SplitType splitType = buffer.readEnumConstant(SplitType.class);
            long time = buffer.readLong();

            sendSplitS2C(server.getPlayerManager().getPlayerList(), splitType, time);
            SpeedRunIGT.debug("hello server split: " + splitType);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void sendSplitS2C(List<ServerPlayerEntity> players, SplitType splitType, long time) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeEnumConstant(splitType);
        passedData.writeLong(time);

        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_SPLIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveSplitS2C(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOptions.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
            SplitType splitType = buffer.readEnumConstant(SplitType.class);
            long time = buffer.readLong();
            client.execute(() -> InGameTimer.getInstance().getTimerSplit().tryUpdateSplit(splitType, time, false));
            SpeedRunIGT.debug("hello client split: " + splitType);
        } catch (Exception e) {
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }
}
