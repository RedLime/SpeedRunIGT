package com.redlimerl.speedrunigt.timer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.ServerStatHandlerAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.timer.logs.TimerPauseLog;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.class_2750;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InGameTimerUtils {
    public static boolean IS_CHANGING_DIMENSION = false;
    public static boolean IS_CAN_WAIT_WORLD_LOAD = false;
    public static final HashSet<Object> CHANGED_OPTIONS = Sets.newHashSet();
    public static boolean RETIME_IS_WAITING_LOAD = false;
    public static boolean IS_SET_SEED = false;

    public static File getTimerLogDir(String name, String pathName) {
        File file;
        if (SpeedRunIGT.IS_CLIENT_SIDE) {
            file = FabricLoader.getInstance().getGameDir().resolve("saves").resolve(name).resolve(SpeedRunIGT.MOD_ID).resolve(pathName).toFile();
        } else {
            file = FabricLoader.getInstance().getGameDir().resolve("world").resolve(SpeedRunIGT.MOD_ID).resolve(pathName).toFile();
        }
        if (!file.exists()) SpeedRunIGT.debug(file.mkdirs() ? "make timer dirs" : "failed to make timer dirs");
        return file;
    }

    public static boolean isWaitingFirstInput() {
        return SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).isFirstInput(InGameTimer.getInstance());
    }

    public static String logListToString(ArrayList<?> arrayList, int completeCount) {
        if (arrayList.size() == 0) return "";
        StringBuilder stringBuilder = new StringBuilder();
        if (completeCount > 0) {
            stringBuilder.append("/* The timer/log is segmented. If you need previous logs, check the igt_freeze").append(InGameTimer.getLogSuffix(completeCount)).append(" file.").append(" */\n");
        }
        for (Object o : arrayList) {
            stringBuilder.append(o.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    private static String makeLogText(int length, Object text) {
        String empty = IntStream.range(0, Math.max(0, length - text.toString().length())).mapToObj(i -> " ").collect(Collectors.joining());
        return text + empty;
    }

    public static String pauseLogListToString(List<TimerPauseLog> arrayList, boolean makeHeader, int completeCount) {
        if (arrayList.size() == 0) return "";

        StringBuilder stringBuilder = new StringBuilder();
        if (completeCount > 0) {
            stringBuilder.append("/* The timer/log is segmented. If you need previous logs, check the igt_timer").append(InGameTimer.getLogSuffix(completeCount)).append(" file.").append(" */\n");
        }
        if (makeHeader) {
            stringBuilder.append(makeLogText(5, "No"))
                    .append(makeLogText(15, "IGT"))
                    .append(makeLogText(15, "Start RTA"))
                    .append(makeLogText(15, "End RTA"))
                    .append(makeLogText(11, "Length"))
                    .append(makeLogText(11, "Retime?"))
                    .append("Reason / Notice")
                    .append("\n");
        }

        for (TimerPauseLog pause : arrayList) {
            stringBuilder
                    .append(makeLogText(5, pause.getPauseCount()))
                    .append(makeLogText(15, timeToStringFormat(pause.getIGT())))
                    .append(makeLogText(15, timeToStringFormat(pause.getUnpauseRTA() - pause.getPauseLength())))
                    .append(makeLogText(15, timeToStringFormat(pause.getUnpauseRTA())))
                    .append(makeLogText(11, millisecondToStringFormat(pause.getPauseLength())))
                    .append(makeLogText(11, pause.getRetimeData().getRetimeNeedAmount() == 0 ? "" : millisecondToStringFormat(pause.getRetimeData().getRetimeNeedAmount())))
                    .append(String.format("Paused by %s, Unpause by %s%s", pause.getPauseReason(), pause.getUnpauseReason(), pause.getRetimeData().getNoticeInfo().isEmpty() ? "" : " / "+pause.getRetimeData().getNoticeInfo()))
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
        return String.format("%d.%03d", seconds, time % 1000);
    }

    public static JsonObject convertTimelineJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mc_version", getMinecraftVersion());
        jsonObject.addProperty("speedrunigt_version", SpeedRunIGT.MOD_VERSION);
        jsonObject.addProperty("category", timer.getCategory().getID());
        jsonObject.addProperty("run_type", timer.getRunType().getContext());
        jsonObject.addProperty("is_completed", timer.isCompleted());
        jsonObject.addProperty("is_coop", timer.isCoop());
        jsonObject.addProperty("is_hardcore", timer.isHardcore());
        jsonObject.addProperty("is_legacy_igt", timer.isLegacyIGT());
        jsonObject.addProperty("world_name", timer.worldName);
        jsonObject.addProperty("date", System.currentTimeMillis());
        jsonObject.addProperty("retimed_igt", timer.getRetimedInGameTime());
        jsonObject.addProperty("final_igt", timer.getInGameTime(false));
        jsonObject.addProperty("final_rta", timer.getRealTimeAttack());
        if (timer.lanOpenedTime == null) jsonObject.add("open_lan", JsonNull.INSTANCE);
        else jsonObject.addProperty("open_lan", timer.lanOpenedTime);
        JsonArray timelineArr = new JsonArray();
        for (TimerTimeline timeline : timer.getTimelines()) {
            JsonObject timelineObj = new JsonObject();
            timelineObj.addProperty("name", timeline.getName());
            timelineObj.addProperty("igt", timeline.getIGT());
            timelineObj.addProperty("rta", timeline.getRTA());
            timelineArr.add(timelineObj);
        }
        jsonObject.add("timelines", timelineArr);
        jsonObject.add("advancements", SpeedRunIGT.GSON.toJsonTree(timer.getAdvancementsTracker().getAdvancements()));
        jsonObject.add("stats", getStatsJson(timer));

        return jsonObject;
    }

    public static JsonObject getStatsJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        MinecraftServer server = getServer();
        if (timer.isServerIntegrated && server != null && server.getPlayerManager() != null) {
            ArrayList<ServerPlayerEntity> serverPlayerEntities = Lists.newArrayList(server.getPlayerManager().getPlayers());
            for (ServerPlayerEntity serverPlayerEntity : serverPlayerEntities) {
                jsonObject.add(serverPlayerEntity.getUuid().toString(), SpeedRunIGT.GSON.fromJson(ServerStatHandler.method_8272(((ServerStatHandlerAccessor) serverPlayerEntity.getStatHandler()).getStatMap()), JsonObject.class));
            }
        }
        return jsonObject;
    }

    public static boolean isHardcoreWorld() {
        if (SpeedRunIGT.IS_CLIENT_SIDE) return InGameTimerClientUtils.isHardcoreWorld();
        return SpeedRunIGT.DEDICATED_SERVER.isHardcore();
    }

    public static String getMinecraftVersion() {
        return FabricLoaderImpl.INSTANCE.getGameProvider().getNormalizedGameVersion();
    }

    public static boolean isLoadableBlind(class_2750 dimensionType, Vec3d netherPos, Vec3d overPos) {
        InGameTimer timer = InGameTimer.getInstance();
        ArrayList<RunPortalPos> arrayList = dimensionType == class_2750.field_12921 ? timer.lastNetherPortalPos : dimensionType == class_2750.field_12920 ? timer.lastOverWorldPortalPos : null;
        Vec3d targetPos = dimensionType == class_2750.field_12921 ? netherPos : dimensionType == class_2750.field_12920 ? overPos : null;
        if (arrayList == null || targetPos == null) return true;
        for (RunPortalPos portalPos : arrayList) {
            if (portalPos.squaredDistanceTo(targetPos) < 16) return false;
        }
        timer.lastNetherPortalPos.add(new RunPortalPos(netherPos));
        timer.lastOverWorldPortalPos.add(new RunPortalPos(overPos));
        return true;
    }

    public static boolean isBlindTraveled(Vec3d netherPos) {
        InGameTimer timer = InGameTimer.getInstance();
        for (RunPortalPos portalPos : timer.lastNetherPortalPos) {
            if (portalPos.squaredDistanceTo(netherPos) < 16) return false;
        }
        return true;
    }

    public static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (SpeedRunIGT.IS_CLIENT_SIDE) {
            InGameTimerClientUtils.setCategoryWarningScreen(conditionFileName, exception);
        }
        SpeedRunIGT.error(exception.getDetails());
    }

    public static MinecraftServer getServer() {
        return SpeedRunIGT.IS_CLIENT_SIDE ? InGameTimerClientUtils.getClientServer() : SpeedRunIGT.DEDICATED_SERVER;
    }
}
