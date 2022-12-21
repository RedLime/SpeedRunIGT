package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.category.RunCategoryBuilder;
import com.redlimerl.speedrunigt.timer.running.RunType;

public class PracticeTimerManager {

    public static final RunCategory PRACTICE_CATEGORY = RunCategoryBuilder.create("pratice_world", "", "Practice").setHideCategory(true).build();


    public static void startPractice(float offsetTime) {
        if ((InGameTimer.getInstance().isPlaying() && !InGameTimer.getInstance().isCompleted()) && InGameTimer.getInstance().getCategory() == PracticeTimerManager.PRACTICE_CATEGORY)
            return;
        String worldName = InGameTimer.getInstance().worldName;
        InGameTimer.start(worldName, RunType.OLD_WORLD);
        InGameTimer.getInstance().setRTAMode(true);
        InGameTimer.getInstance().setStartTime(System.currentTimeMillis() - (long) (offsetTime * 1000));
        InGameTimer.getInstance().setWriteFiles(false);
        InGameTimer.getInstance().setCategory(PRACTICE_CATEGORY, false);
    }

    public static void stopPractice() {
        if (InGameTimer.getInstance().getCategory() == PRACTICE_CATEGORY)
            InGameTimer.complete();
    }
}
