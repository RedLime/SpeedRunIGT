package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

    @Inject(at = @At("HEAD"), method = "createLevelFromExistingSettings")
    public void onCreate(LevelStorageSource.LevelStorageAccess session, ReloadableServerResources dataPackContents, LayeredRegistryAccess<RegistryLayer> dynamicRegistryManager, WorldData saveProperties, CallbackInfo ci) {
        RunCategory category = SpeedRunOption.getOption(SpeedRunOptions.TIMER_CATEGORY);
        if (category.isAutoStart()) {
            InGameTimer.start(session.getLevelId(), RunType.fromBoolean(InGameTimerUtils.IS_SET_SEED));
            InGameTimer.getInstance().setDefaultGameMode(saveProperties.getLevelSettings().gameType().getId());
            InGameTimer.getInstance().setCheatAvailable(saveProperties.getLevelSettings().allowCommands());
            InGameTimer.getInstance().checkDifficulty(saveProperties.getDifficulty());
        }
        InGameTimerUtils.IS_CHANGING_DIMENSION = true;
        InGameTimerUtils.CAN_DISCONNECT = false;
    }

    @Inject(at = @At("HEAD"), method = "openWorld(Ljava/lang/String;Ljava/lang/Runnable;)V")
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
