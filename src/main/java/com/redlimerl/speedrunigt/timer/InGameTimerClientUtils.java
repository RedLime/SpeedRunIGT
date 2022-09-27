package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.gui.screen.FailedCategoryInitScreen;
import com.redlimerl.speedrunigt.mixins.access.ClientChunkProviderAccessor;
import com.redlimerl.speedrunigt.mixins.access.MinecraftClientAccessorForAttack;
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
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

@Environment(EnvType.CLIENT)
public class InGameTimerClientUtils {

    public static boolean canUnpauseTimer(boolean checkRender) {
        MinecraftClient client = MinecraftClient.getInstance();
        InGameTimer timer = InGameTimer.getInstance();

        if (timer.getStatus() != TimerStatus.IDLE) return false;

        if (!client.isPaused() && client.worldRenderer != null && Mouse.isInsideWindow() && Display.isActive() && Mouse.isGrabbed()
                && !InGameTimerUtils.IS_CHANGING_DIMENSION) {
            if (checkRender) {
                WorldRendererAccessor worldRenderer = (WorldRendererAccessor) client.worldRenderer;
                if (worldRenderer.getChunkBuilder() == null) return false;
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
            return (float) ((ClientChunkProviderAccessor) client.world.getChunkProvider()).getChunkMap().size() / (chunks * chunks);
        }
        return 0;
    }

    public static boolean isHardcoreWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.world.getLevelProperties().isHardcore();
    }

    public static Long getPlayerTime() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (server != null && player != null) {
            ServerStatHandler statHandler = server.getPlayerManager().createStatHandler(player);
            return statHandler == null ? null : statHandler.method_1729(Stats.MINUTES_PLAYED) * 50L;
        }
        return null;
    }

    public static @Nullable FailedCategoryInitScreen FAILED_CATEGORY_INIT_SCREEN = null;
    static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (MinecraftClient.getInstance().currentScreen == null)
            FAILED_CATEGORY_INIT_SCREEN = new FailedCategoryInitScreen(conditionFileName, exception);
        else MinecraftClient.getInstance().openScreen(new FailedCategoryInitScreen(conditionFileName, exception));
    }

    static MinecraftServer getClientServer() {
        return MinecraftClient.getInstance().getServer();
    }

    public static boolean isFocusedClick() {
        return MinecraftClient.getInstance().player != null && !MinecraftClient.getInstance().player.method_13061()
                && ((MinecraftClientAccessorForAttack) MinecraftClient.getInstance()).getAttackCoolDown() <= 0;
    }
}
