package com.redlimerl.speedrunigt.timer.packet;

import com.redlimerl.speedrunigt.timer.packet.packets.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class TimerPackets {
    public static void init() {
        registerPacket(TimerInitializePacket.IDENTIFIER, TimerInitializePacket.CODEC);
        registerPacket(TimerStartPacket.IDENTIFIER, TimerStartPacket.CODEC);
        registerPacket(TimerCompletePacket.IDENTIFIER, TimerCompletePacket.CODEC);
        registerPacket(TimerUncompletedPacket.IDENTIFIER, TimerUncompletedPacket.CODEC);
        registerPacket(TimerChangeCategoryPacket.IDENTIFIER, TimerChangeCategoryPacket.CODEC);
        registerPacket(TimerDataConditionPacket.IDENTIFIER, TimerDataConditionPacket.CODEC);
        registerPacket(TimerCustomConditionPacket.IDENTIFIER, TimerCustomConditionPacket.CODEC);
        registerPacket(TimerTimelinePacket.IDENTIFIER, TimerTimelinePacket.CODEC);
        registerPacket(TimerAchieveAdvancementPacket.IDENTIFIER, TimerAchieveAdvancementPacket.CODEC);
        registerPacket(TimerAchieveCriteriaPacket.IDENTIFIER, TimerAchieveCriteriaPacket.CODEC);
    }
    private static <T extends TimerPacket<?>> void registerPacket(CustomPayload.Id<T> id, PacketCodec<RegistryByteBuf, T> codec) {
        PayloadTypeRegistry.playC2S().register(id, codec);
        PayloadTypeRegistry.playS2C().register(id, codec);
        ServerPlayNetworking.registerGlobalReceiver(id,
                (payload, context) -> payload.receiveClient2ServerPacket(context.player().getServer()));
        ClientPlayNetworking.registerGlobalReceiver(id,
                (payload, context) -> payload.receiveServer2ClientPacket(context.client()));
    }

}
