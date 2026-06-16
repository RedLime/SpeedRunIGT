package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.gui.screen.FailedCategoryInitScreen;
import com.redlimerl.speedrunigt.mixins.access.LevelRendererAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftAccessorForAttack;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class InGameTimerClientUtils {

    public static boolean canUnpauseTimer(boolean checkRender) {
        Minecraft client = Minecraft.getInstance();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!client.isPaused() && client.isWindowActive() && client.mouseHandler.isMouseGrabbed() && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
            if (checkRender) {
                LevelRendererAccessor worldRenderer = (LevelRendererAccessor) client.levelRenderer;
                int chunks = client.levelExtractor.countRenderedSections();
                int entities = worldRenderer.srigt$getLevelRenderState().entityRenderStates.size() - (client.options.getCameraType().isFirstPerson() ? 0 : 1);

                return chunks + entities > 0;
            }
            return true;
        }
        return false;
    }

    public static float getGeneratedChunkRatio() {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            int chunks = client.options.renderDistance().get() * 2 + 1;
            return (float) client.level.getChunkSource().getLoadedChunksCount() / (chunks*chunks);
        }
        return 0;
    }

    public static boolean isHardcoreWorld() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.player.level().getLevelData().isHardcore();
    }

    public static Long getPlayerTime() {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        Player player = Minecraft.getInstance().player;
        if (server != null && player != null) {
            ServerStatsCounter statHandler = server.getPlayerList().getPlayerStats(player);
            return statHandler == null ? null : statHandler.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) * 50L;
        }
        return null;
    }

    public static @Nullable FailedCategoryInitScreen FAILED_CATEGORY_INIT_SCREEN = null;
    static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (Minecraft.getInstance().gui.screen() == null)
            FAILED_CATEGORY_INIT_SCREEN = new FailedCategoryInitScreen(conditionFileName, exception);
        else Minecraft.getInstance().gui.setScreen(new FailedCategoryInitScreen(conditionFileName, exception));
    }

    static MinecraftServer getClientServer() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    public static boolean isFocusedClick() {
        return Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isUsingItem()
                && ((MinecraftAccessorForAttack) Minecraft.getInstance()).getAttackCoolDown() <= 0;
    }
}
