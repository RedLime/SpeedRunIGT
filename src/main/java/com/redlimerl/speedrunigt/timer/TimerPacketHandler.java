package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
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

    private static final MinecraftClient client = MinecraftClient.getInstance();

    /*
    Timer init packets
     */
    public static void sendInitC2S(InGameTimer timer) {
        sendInitC2S(timer.startTime, timer.getCategory());
    }

    public static void sendInitC2S(long time, RunCategory category) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(time);
        passedData.writeEnumConstant(category);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_INIT_ID, passedData));
    }

    public static void receiveInitC2S(MinecraftServer server, PacketByteBuf buffer) {
        long startTime = buffer.readLong();
        RunCategory category = buffer.readEnumConstant(RunCategory.class);

        sendInitS2C(server.getPlayerManager().getPlayerList(), startTime, category);
        SpeedRunIGT.debug("server received init: " + startTime + " / " + category.name());
    }

    public static void sendInitS2C(List<ServerPlayerEntity> players, long startTime, RunCategory category) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(startTime);
        passedData.writeEnumConstant(category);
        CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_INIT_ID, passedData);

        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(s2CPacket);
        }
    }

    public static void receiveInitS2C(PacketByteBuf buffer) {
        long startTime = buffer.readLong();
        RunCategory category = buffer.readEnumConstant(RunCategory.class);

        client.execute(() -> {
            InGameTimer.start();
            InGameTimer.getInstance().isCoop = true;
            InGameTimer.getInstance().isServerIntegrated = MinecraftClient.getInstance().isIntegratedServerRunning();
            InGameTimer.getInstance().startTime = startTime;
            InGameTimer.getInstance().setCategory(category);
            InGameTimer.getInstance().setPause(false);
        });

        SpeedRunIGT.debug("client received init: " + startTime + " / " + category.name());
    }


    /*
    Timer complete packets
     */
    public static void sendCompleteC2S(InGameTimer timer) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeLong(timer.endTime);

        if (client.getNetworkHandler() != null)
            client.getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_COMPLETE_ID, passedData));
    }

    public static void receiveCompleteC2S(MinecraftServer server, PacketByteBuf buffer) {
        long endTime = buffer.readLong();

        sendCompleteS2C(server.getPlayerManager().getPlayerList(), endTime);
        SpeedRunIGT.debug("hello server complete: " + endTime);
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
        long endTime = buffer.readLong();
        client.execute(() -> InGameTimer.complete(endTime));
        SpeedRunIGT.debug("hello client complete: " + endTime);
    }
}
