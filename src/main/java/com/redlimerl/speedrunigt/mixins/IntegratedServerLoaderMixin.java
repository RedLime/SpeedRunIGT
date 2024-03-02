package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {

    @Inject(at = @At("HEAD"), method = "startNewWorld")
    public void onCreate(LevelStorage.Session session, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistryManager, SaveProperties saveProperties, CallbackInfo ci) {
        RunCategory category = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY);
        if (category.isAutoStart()) {
            InGameTimer.start(session.getDirectoryName(), RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
            InGameTimer.getInstance().setDefaultGameMode(saveProperties.getLevelInfo().getGameMode().getId());
            InGameTimer.getInstance().setCheatAvailable(saveProperties.getLevelInfo().areCommandsAllowed());
            InGameTimer.getInstance().checkDifficulty(saveProperties.getDifficulty());
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        InGameTimerUtils.CAN_DISCONNECT = false;
    }

    @Inject(at = @At("HEAD"), method = "start(Ljava/lang/String;Ljava/lang/Runnable;)V")
    public void onWorldOpen(String levelName, Runnable onCancel, CallbackInfo ci) {
        try {
            boolean loaded = InGameTimer.load(levelName);
            if (!loaded) InGameTimer.end();
        } catch (Exception e) {
            InGameTimer.end();
            SpeedRunIGT.error("Exception in timer load, can't load the timer.");
            e.printStackTrace();
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        InGameTimerUtils.CAN_DISCONNECT = false;
    }
}
