package com.redlimerl.speedrunigt.timer;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redlimerl.speedrunigt.gui.screen.FailedCategoryInitScreen;
import com.redlimerl.speedrunigt.mixins.access.ClientChunkProviderAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessorForAttack;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class InGameTimerClientUtils {
    public static JsonObject STATS_UPDATE = null;

    public static boolean canUnpauseTimer(boolean checkRender) {
        Minecraft client = Minecraft.getMinecraft();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!((MinecraftClientAccessorForAttack) client).isPaused() && client.worldRenderer != null && Mouse.isInsideWindow() && Display.isActive() && Mouse.isGrabbed()
                && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
            if (checkRender) {
                WorldRendererAccessor worldRenderer = (WorldRendererAccessor) client.worldRenderer;
                int chunks = worldRenderer.getCompletedChunkCount();
                int entities = worldRenderer.getRegularEntityCount() - (client.options.perspective > 0 ? 0 : 1);

                return chunks + entities > 0;
            }
            return true;
        }
        return false;
    }


    public static float getGeneratedChunkRatio() {
        Minecraft client = Minecraft.getMinecraft();
        if (client.world != null && client.playerEntity != null) {
            int chunks = (16  >> client.options.renderDistance) * 2 + 1;
            return (float) ((ClientChunkProviderAccessor) client.world.getChunkProvider()).getChunkMap().getUsedEntriesCount() / (chunks * chunks);
        }
        return 0;
    }

    public static boolean isHardcoreWorld() {
        Minecraft client = Minecraft.getMinecraft();
        return client.playerEntity != null && client.playerEntity.world.getLevelProperties().isHardcore();
    }

    public static Long getPlayerTime() {
        StatHandler statHandler = Minecraft.getMinecraft().statHandler;
        return statHandler == null ? null : statHandler.getStatLevel(Stats.MINUTES_PLAYED) * 50L;
    }

    public static @Nullable FailedCategoryInitScreen FAILED_CATEGORY_INIT_SCREEN = null;
    static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (Minecraft.getMinecraft().currentScreen == null)
            FAILED_CATEGORY_INIT_SCREEN = new FailedCategoryInitScreen(conditionFileName, exception);
        else Minecraft.getMinecraft().openScreen(new FailedCategoryInitScreen(conditionFileName, exception));
    }

    static MinecraftServer getClientServer() {
        return Minecraft.getMinecraft().getServer();
    }

    public static boolean isFocusedClick() {
        return ((MinecraftClientAccessorForAttack) Minecraft.getMinecraft()).getAttackCoolDown() <= 0;
    }


    public static JsonObject getStatsJson(InGameTimer timer) {
        return timer.isServerIntegrated && STATS_UPDATE != null ? STATS_UPDATE : new JsonObject();
    }

    public static void updateStatsJson(InGameTimer timer) {
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonObject2 = new JsonObject();
        jsonObject.add(Minecraft.getMinecraft().playerEntity.username, jsonObject2);
        if (timer.isServerIntegrated) {
            StatHandler stats = Minecraft.getMinecraft().statHandler;
            for (Object object : stats.method_1734().entrySet()) {
                @SuppressWarnings("unchecked")
                Map.Entry<Stat, Integer> statEntry = (Map.Entry<Stat, Integer>) object;
                jsonObject2.add(statEntry.getKey().getStringId(), new JsonPrimitive(statEntry.getValue()));
            }
        }
        STATS_UPDATE = jsonObject;
    }
}
