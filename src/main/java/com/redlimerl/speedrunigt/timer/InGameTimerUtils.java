package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.mixins.access.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

public class InGameTimerUtils {
    public static boolean IS_CHANGING_DIMENSION = false;

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

}
