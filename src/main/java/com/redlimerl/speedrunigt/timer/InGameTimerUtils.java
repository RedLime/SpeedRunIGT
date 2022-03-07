package com.redlimerl.speedrunigt.timer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.LevelStorageAccessor;
import com.redlimerl.speedrunigt.mixins.access.ServerStatHandlerAccessor;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import com.redlimerl.speedrunigt.utils.MixinValues;
import net.minecraft.client.MinecraftClient;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.nio.file.Path;
import java.util.ArrayList;

public class InGameTimerUtils {
    public static boolean IS_CHANGING_DIMENSION = false;

    public static Path getWorldSavePath(String name) {
        return ((LevelStorageAccessor) MinecraftClient.getInstance().getCurrentSave()).getFile().toPath().resolve(name);
    }

    public static boolean canUnpauseTimer(boolean checkRender) {
        MinecraftClient client = MinecraftClient.getInstance();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!client.isPaused() && client.worldRenderer != null && Mouse.isInsideWindow() && Display.isActive() && Mouse.isGrabbed()
                && !IS_CHANGING_DIMENSION) {
            if (checkRender) {
                WorldRendererAccessor worldRenderer = (WorldRendererAccessor) client.worldRenderer;
                client.worldRenderer.getChunksDebugString(); // For init MixinValues#completedChunks value
                int chunks = MixinValues.COMPLETED_RENDER_CHUNKS;
                int entities = worldRenderer.getRegularEntityCount() - (client.options.perspective > 0 ? 0 : 1);

                return chunks + entities > 0;
            }
            return true;
        }
        return false;
    }

    public static String logListToString(ArrayList<?> arrayList) {
        if (arrayList.size() == 0) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : arrayList) {
            stringBuilder.append(o.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public static String timeToStringFormat(long time) {
        int seconds = ((int) (time / 1000)) % 60;
        int minutes = ((int) (time / 1000)) / 60;
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, time % 1000);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, time % 1000);
        }
    }

    public static JsonObject convertTimelineJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mc_version", getMinecraftVersion());
        jsonObject.addProperty("speedrunigt_version", SpeedRunIGT.MOD_VERSION);
        jsonObject.addProperty("category", timer.getCategory().getID());
        jsonObject.addProperty("is_coop", timer.isCoop());
        jsonObject.addProperty("is_hardcore", timer.isHardcore());
        jsonObject.addProperty("is_legacy_igt", timer.isLegacyIGT());
        jsonObject.addProperty("date", System.currentTimeMillis());
        jsonObject.addProperty("final_igt", timer.getInGameTime(false));
        jsonObject.addProperty("final_rta", timer.getRealTimeAttack());
        JsonArray timelineArr = new JsonArray();
        for (TimerTimeline timeline : timer.getTimelines()) {
            JsonObject timelineObj = new JsonObject();
            timelineObj.addProperty("name", timeline.getName());
            timelineObj.addProperty("igt", timeline.getIGT());
            timelineObj.addProperty("rta", timeline.getRTA());
            timelineArr.add(timelineObj);
        }
        jsonObject.add("timeline", timelineArr);
        jsonObject.add("stats", getStatsJson(timer));

        return jsonObject;
    }

    public static JsonObject getStatsJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (timer.isServerIntegrated && server != null && server.getPlayerManager() != null) {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayers()) {
                jsonObject.add(serverPlayerEntity.getUuid().toString(), SpeedRunIGT.GSON.fromJson(ServerStatHandler.method_8272(((ServerStatHandlerAccessor) serverPlayerEntity.getStatHandler()).getStatMap()), JsonObject.class));
            }
        }
        return jsonObject;
    }

    public static boolean isHardcoreWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.world.getLevelProperties().isHardcore();
    }

    public static String getMinecraftVersion() {
        return RealmsSharedConstants.VERSION_STRING;
    }
}
