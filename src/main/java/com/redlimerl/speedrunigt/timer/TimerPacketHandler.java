package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TimerPacketHandler {

    public static final Identifier PACKET_TIMER_ID = new Identifier(SpeedRunIGT.MOD_ID, "timer");


    public static void clientSend(InGameTimer instance, InGameTimer completeInstance) {
        if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer())
                .writeByteArray(InGameTimerUtils.serializeTimer(instance))
                .writeByteArray(InGameTimerUtils.serializeTimer(completeInstance));

        if (MinecraftClient.getInstance().getNetworkHandler() != null)
            MinecraftClient.getInstance().getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(PACKET_TIMER_ID, passedData));
    }

    public static void serverReceiveAndSend(MinecraftServer server, PacketByteBuf buffer) {
        try {
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer())
                    .writeByteArray(buffer.readByteArray())
                    .writeByteArray(buffer.readByteArray())
                    .writeString(InGameTimer.getInstance().uuid.toString());

            CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_ID, passedData);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.networkHandler.sendPacket(s2CPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void serverSend(List<ServerPlayerEntity> players, InGameTimer instance, InGameTimer completeInstance) {
        try {
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer())
                    .writeByteArray(InGameTimerUtils.serializeTimer(instance))
                    .writeByteArray(InGameTimerUtils.serializeTimer(completeInstance))
                    .writeString(InGameTimer.getInstance().uuid.toString());

            CustomPayloadS2CPacket s2CPacket = new CustomPayloadS2CPacket(PACKET_TIMER_ID, passedData);

            for (ServerPlayerEntity player : players) {
                player.networkHandler.sendPacket(s2CPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }

    public static void clientReceive(PacketByteBuf buffer) {
        try {
            if (!SpeedRunOption.getOption(SpeedRunOptions.AUTOMATIC_COOP_MODE)) return;

            InGameTimer instance = InGameTimerUtils.deserializeTimer(buffer.readByteArray());
            InGameTimer completeInstance = InGameTimerUtils.deserializeTimer(buffer.readByteArray());
            String uuidString = InGameTimer.getInstance().uuid.toString();

            boolean isCompletedBefore = InGameTimer.getInstance().isCompleted();
            boolean isServerMember = !Objects.equals(uuidString, instance.uuid.toString());
            if (isServerMember) {
                instance.uuid = UUID.fromString(uuidString);
                completeInstance.uuid = instance.uuid;
                InGameTimer.INSTANCE = instance;
                InGameTimer.COMPLETED_INSTANCE = completeInstance;
            }
            InGameTimer.getInstance().isCoop = true;
            InGameTimer.getInstance().isServerIntegrated = !isServerMember;
            InGameTimer.getInstance().setPause(false, "co-op setup");
            SpeedRunIGT.debug("Client Side Received : " + InGameTimer.getInstance().isServerIntegrated);
            if (!isCompletedBefore && InGameTimer.getInstance().isCompleted())
                InGameTimer.complete(InGameTimer.getCompletedInstance().endTime);
        } catch (Exception e) {
            e.printStackTrace();
            SpeedRunIGT.error("Failed read packets, probably SpeedRunIGT version different between players");
        }
    }
}
