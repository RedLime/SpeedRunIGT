package com.redlimerl.speedrunigt.timer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.ClientChunkProviderAccessor;
import com.redlimerl.speedrunigt.mixins.access.ServerStatHandlerAccessor;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.timer.logs.TimerPauseLog;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InGameTimerUtils {
    public static boolean IS_CHANGING_DIMENSION = false;
    public static boolean RETIME_IS_CHANGED_OPTION = false;
    public static boolean RETIME_IS_WAITING_LOAD = false;

    public static Path getWorldSavePath(String name) {
        return MinecraftClient.getInstance().getCurrentSave().method_17969(name);
    }

    public static boolean canUnpauseTimer(boolean checkRender) {
        MinecraftClient client = MinecraftClient.getInstance();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!client.isPaused() && client.worldRenderer != null && client.isWindowFocused() && client.field_19945.method_18252()
                && !IS_CHANGING_DIMENSION) {
            if (checkRender) {
                WorldRendererAccessor worldRenderer = (WorldRendererAccessor) client.worldRenderer;
                int chunks = worldRenderer.invokeCompletedChunkCount();
                int entities = worldRenderer.getRegularEntityCount() - (client.options.perspective > 0 ? 0 : 1);

                return chunks + entities > 0;
            }
            return true;
        }
        return false;
    }

    public static float getGeneratedChunkRatio() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            int chunks = (client.options.viewDistance * 2 + 1)^2;
            return (float) ((ClientChunkProviderAccessor) client.world.method_3586()).getChunkMap().size() / chunks;
        }
        return 0;
    }

    public static String logListToString(ArrayList<?> arrayList) {
        if (arrayList.size() == 0) return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : arrayList) {
            stringBuilder.append(o.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    private static String makeLogText(int length, Object text) {
        String empty = IntStream.range(0, Math.max(0, length - text.toString().length())).mapToObj(i -> " ").collect(Collectors.joining());
        return text + empty;
    }

    public static String pauseLogListToString(List<TimerPauseLog> arrayList) {
        if (arrayList.size() == 0) return "";

        StringBuilder stringBuilder = new StringBuilder()
                .append(makeLogText(5, "No"))
                .append(makeLogText(15, "IGT"))
                .append(makeLogText(15, "Start RTA"))
                .append(makeLogText(15, "End RTA"))
                .append(makeLogText(11, "Length"))
                .append(makeLogText(11, "Retime"))
                .append("Reason")
                .append("\n");

        for (TimerPauseLog pause : arrayList) {
            stringBuilder
                    .append(makeLogText(5, pause.getPauseCount()))
                    .append(makeLogText(15, timeToStringFormat(pause.getIGT())))
                    .append(makeLogText(15, timeToStringFormat(pause.getUnpauseRTA() - pause.getPauseLength())))
                    .append(makeLogText(15, timeToStringFormat(pause.getUnpauseRTA())))
                    .append(makeLogText(11, millisecondToStringFormat(pause.getPauseLength())))
                    .append(makeLogText(11, pause.getRetimeNeedAmount() == 0 ? "" : millisecondToStringFormat(pause.getRetimeNeedAmount())))
                    .append(String.format("Paused by %s, Unpause by %s", pause.getPauseReason(), pause.getUnpauseReason()))
                    .append("\n");
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

    public static String millisecondToStringFormat(long time) {
        int seconds = (int) (time / 1000);
        return String.format("%d.%d", seconds, time % 1000);
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
        jsonObject.addProperty("retimed_igt", timer.getRetimedInGameTime());
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
                jsonObject.add(serverPlayerEntity.getUuid().toString(), SpeedRunIGT.GSON.fromJson(((ServerStatHandlerAccessor) serverPlayerEntity.getStatHandler()).invokeAsString(), JsonObject.class));
            }
        }
        return jsonObject;
    }

    public static boolean isHardcoreWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.world.method_3588().isHardcore();
    }

    public static String getMinecraftVersion() {
        return RealmsSharedConstants.VERSION_STRING;
    }
}
