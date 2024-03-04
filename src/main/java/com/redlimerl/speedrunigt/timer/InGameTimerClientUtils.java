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
                int chunks = worldRenderer.getCompletedChunkCount();
                int entities = worldRenderer.getRegularEntityCount() - (client.options.perspective > 0 ? 0 : 1);

                return chunks + entities > 0;
            }
            return true;
        }
        return false;
    }


    public static float getGeneratedChunkRatio() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.field_6279 != null) {
            int chunks = client.options.viewDistance * 2 + 1;
            return (float) ((ClientChunkProviderAccessor) client.world.getChunkProvider()).getChunkMap().getUsedEntriesCount() / (chunks * chunks);
        }
        return 0;
    }

    public static boolean isHardcoreWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.field_3805 != null && client.field_3805.world.getLevelProperties().isHardcore();
    }

    public static Long getPlayerTime() {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        PlayerEntity player = MinecraftClient.getInstance().field_3805;
        if (server != null && player != null) {
            ServerStatHandler statHandler = server.getPlayerManager().method_8224(player.getTranslationKey());
            return statHandler == null ? null : statHandler.getStatLevel(Stats.MINUTES_PLAYED) * 50L;
        }
        return null;
    }

    public static @Nullable FailedCategoryInitScreen FAILED_CATEGORY_INIT_SCREEN = null;
    static void setCategoryWarningScreen(@Nullable String conditionFileName, InvalidCategoryException exception) {
        if (MinecraftClient.getInstance().currentScreen == null)
            FAILED_CATEGORY_INIT_SCREEN = new FailedCategoryInitScreen(conditionFileName, exception);
        else MinecraftClient.getInstance().setScreen(new FailedCategoryInitScreen(conditionFileName, exception));
    }

    static MinecraftServer getClientServer() {
        return MinecraftClient.getInstance().getServer();
    }

    public static boolean isFocusedClick() {
        return ((MinecraftClientAccessorForAttack) MinecraftClient.getInstance()).getAttackCoolDown() <= 0;
    }
}
