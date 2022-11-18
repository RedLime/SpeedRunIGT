package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.gui.screen.FailedCategoryInitScreen;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessor;
import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import com.redlimerl.speedrunigt.timer.category.InvalidCategoryException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class InGameTimerClientUtils {
    public static @Nullable FailedCategoryInitScreen FAILED_CATEGORY_INIT_SCREEN = null;

    public static boolean canUnpauseTimer(boolean checkRender) {
        MinecraftClient client = MinecraftClient.getInstance();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!client.isPaused() && client.worldRenderer != null && client.isWindowFocused() && client.mouse.isCursorLocked()
                && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
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
            int chunks = client.options.viewDistance * 2 + 1;
            return (float) client.world.getChunkManager().getLoadedChunkCount() / (chunks*chunks);
        }
        return 0;
    }

    public static boolean isHardcoreWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.world.getLevelProperties().isHardcore();
    }

    public static Long getPlayerTime() {
        MinecraftServer server = getClientServer();
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (server != null && player != null) {
            ServerStatHandler statHandler = server.getPlayerManager().createStatHandler(player);
            return statHandler == null ? null : statHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_ONE_MINUTE)) * 50L;
        }
        return null;
    }

    static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (MinecraftClient.getInstance().currentScreen == null) {
            FAILED_CATEGORY_INIT_SCREEN = new FailedCategoryInitScreen(conditionFileName, exception);
        }
        else MinecraftClient.getInstance().openScreen(new FailedCategoryInitScreen(conditionFileName, exception));
    }

    static MinecraftServer getClientServer() {
        return MinecraftClient.getInstance().getServer();
    }

    public static boolean isFocusedClick() {
        return MinecraftClient.getInstance().player != null
                && !MinecraftClient.getInstance().player.isUsingItem()
                && ((MinecraftClientAccessor) MinecraftClient.getInstance()).getAttackCoolDown() <= 0;
    }
}
