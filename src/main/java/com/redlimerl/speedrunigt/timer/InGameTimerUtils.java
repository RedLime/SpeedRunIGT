package com.redlimerl.speedrunigt.timer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.mixins.access.PlayerManagerAccessor;
import com.redlimerl.speedrunigt.mixins.access.ServerStatHandlerAccessor;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import com.redlimerl.speedrunigt.timer.logs.TimerPauseLog;
import com.redlimerl.speedrunigt.timer.logs.TimerTimeline;
import com.redlimerl.speedrunigt.timer.running.RunPortalPos;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InGameTimerUtils {
    public static boolean IS_CHANGING_DIMENSION = false;
    public static boolean IS_KILLED_ENDER_DRAGON = false;
    public static boolean IS_CAN_WAIT_WORLD_LOAD = false;
    public static final HashSet<Object> CHANGED_OPTIONS = Sets.newHashSet();
    public static JsonObject STATS_UPDATE = null;
    public static boolean RETIME_IS_WAITING_LOAD = false;
    public static boolean IS_SET_SEED = false;
    public static long LATEST_TIMER_TIME = 0;

    public static @Nullable File getTimerLogDir(String worldName, String pathName) {
        Path path;
        if (SpeedRunIGT.IS_CLIENT_SIDE) {
            if (worldName == null || worldName.isEmpty()) return null;
            path = FabricLoader.getInstance().getGameDir().resolve("saves").resolve(worldName);
        } else {
            path = FabricLoader.getInstance().getGameDir().resolve("world");
        }

        File worldFolder = path.toFile();
        File file = path.resolve(SpeedRunIGT.MOD_ID).resolve(pathName).toFile();

        if (!worldFolder.exists() || !worldFolder.isDirectory()) {
            SpeedRunIGT.error("World directory doesn't exist, couldn't make timer dirs");
            return null;
        }

        if (!file.exists()) {
            SpeedRunIGT.debug(file.mkdirs() ? "make timer dirs" : "failed to make timer dirs");
        } else if (!file.isDirectory()) {
            return null;
        }
        return file;
    }

    public static boolean isWaitingFirstInput() {
        return SpeedRunOption.getOption(SpeedRunOptions.WAITING_FIRST_INPUT).isFirstInput(InGameTimer.getInstance());
    }

    public static String logListToString(List<?> arrayList, int completeCount) {
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

    public synchronized static JsonObject convertTimelineJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mc_version", getMinecraftVersion());
        jsonObject.addProperty("speedrunigt_version", SpeedRunIGT.MOD_VERSION);
        jsonObject.addProperty("category", timer.getCategory().getID());
        jsonObject.addProperty("run_type", timer.getRunType().getContext());
        jsonObject.addProperty("is_completed", timer.isCompleted());
        jsonObject.addProperty("is_coop", timer.isCoop());
        jsonObject.addProperty("is_hardcore", timer.isHardcore());
        jsonObject.addProperty("world_name", timer.worldName);
        jsonObject.addProperty("is_cheat_allowed", timer.isCheatAvailable());
        jsonObject.addProperty("default_gamemode", timer.getDefaultGameMode());
        jsonObject.addProperty("date", System.currentTimeMillis());
        jsonObject.addProperty("retimed_igt", timer.getRetimedInGameTime());
        jsonObject.addProperty("final_igt", timer.getInGameTime(false));
        jsonObject.addProperty("stats_igt", timer.getCompleteStatIGT());
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
        jsonObject.add("advancements", SpeedRunIGT.GSON.toJsonTree(sortMapByValue(timer.getAdvancementsTracker().getAdvancements())));
        jsonObject.add("stats", getStatsJson(timer));

        return jsonObject;
    }

    public static LinkedHashMap<String, TimerAdvancementTracker.AdvancementTrack> sortMapByValue(Map<String, TimerAdvancementTracker.AdvancementTrack> map) {
        List<Map.Entry<String, TimerAdvancementTracker.AdvancementTrack>> entries = new LinkedList<>(map.entrySet());
        entries.sort(Comparator.comparingLong(value -> value.getValue().getRTA()));

        LinkedHashMap<String, TimerAdvancementTracker.AdvancementTrack> result = new LinkedHashMap<>();
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static JsonObject getStatsJson(InGameTimer timer) {
        return timer.isServerIntegrated && STATS_UPDATE != null ? STATS_UPDATE : new JsonObject();
    }

    public static void updateStatsJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        MinecraftServer server = getServer();
        if (timer.isServerIntegrated && server != null && server.getPlayerManager() != null) {
            ArrayList<ServerPlayerEntity> serverPlayerEntities = Lists.newArrayList(server.getPlayerManager().getPlayers());
            for (ServerPlayerEntity serverPlayerEntity : serverPlayerEntities) {
                jsonObject.add(serverPlayerEntity.getUuid().toString(), SpeedRunIGT.GSON.fromJson(ServerStatHandler.method_8272(((ServerStatHandlerAccessor) serverPlayerEntity.getStatHandler()).getStatMap()), JsonObject.class));
            }
        }
        STATS_UPDATE = jsonObject;
    }

    public static boolean isHardcoreWorld() {
        if (SpeedRunIGT.IS_CLIENT_SIDE) return InGameTimerClientUtils.isHardcoreWorld();
        return SpeedRunIGT.DEDICATED_SERVER.isHardcore();
    }

    public static String getMinecraftVersion() {
        Optional<ModContainer> mcContainer = FabricLoader.getInstance().getModContainer("minecraft");
        if (mcContainer.isPresent()) {
            ModContainer mc = mcContainer.get();
            return mc.getMetadata().getVersion().getFriendlyString();
        }
        return "unknown";
    }

    public static boolean isLoadableBlind(DimensionType dimensionType, Vec3d netherPos, Vec3d overPos) {
        InGameTimer timer = InGameTimer.getInstance();
        List<RunPortalPos> arrayList = dimensionType == DimensionType.NETHER ? timer.lastNetherPortalPos : dimensionType == DimensionType.OVERWORLD ? timer.lastOverWorldPortalPos : null;
        Vec3d targetPos = dimensionType == DimensionType.NETHER ? netherPos : dimensionType == DimensionType.OVERWORLD ? overPos : null;
        if (arrayList == null || targetPos == null) return true;
        for (RunPortalPos portalPos : arrayList) {
            if (portalPos.squaredDistanceTo(targetPos) < 16) return false;
        }
        timer.lastNetherPortalPos.add(new RunPortalPos(netherPos));
        timer.lastOverWorldPortalPos.add(new RunPortalPos(overPos));
        return true;
    }

    public static int isBlindTraveled(Vec3d netherPos) {
        InGameTimer timer = InGameTimer.getInstance();
        for (int i = 0; i < timer.lastNetherPortalPos.size(); i++) {
            if (timer.lastNetherPortalPos.get(i).squaredDistanceTo(netherPos) < 16) return i;
        }
        return -1;
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

    public static int getCurrentWorldDefaultGameMode() {
        MinecraftServer server = getServer();
        if (server == null) return GameMode.SURVIVAL.getGameModeId();
        return server.method_3026().getGameModeId();
    }

    public static boolean isCurrentWorldCheatAvailable() {
        MinecraftServer server = getServer();
        if (server == null) return false;
        return ((PlayerManagerAccessor) server.getPlayerManager()).isCheatsAllowedInject();
    }
}
