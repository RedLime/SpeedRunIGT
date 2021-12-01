package com.redlimerl.speedrunigt.timer;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.crypt.Crypto;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * In-game Timer class.
 * {@link TimerStatus}
 */
@SuppressWarnings("unused")
public class InGameTimer {

    @NotNull
    private static InGameTimer INSTANCE = new InGameTimer();

    public static InGameTimer getInstance() { return INSTANCE; }
    public static String currentWorldName = "";

    private static final ArrayList<Consumer<InGameTimer>> onCompleteConsumers = new ArrayList<>();

    public static void onComplete(Consumer<InGameTimer> supplier) {
        onCompleteConsumers.add(supplier);
    }

    private RunCategory category = RunCategory.ANY;

    //Timer time
    private long startTime = 0;
    private long endTime = 0;
    private long rebaseIGTime = 0;
    private long rebaseRealTime = 0;
    private long activateTicks = 0;
    private long leastTickTime = 0;
    private long leastStartTime = 0;
    private int throwFPSTick = 0;

    //Logs
    private String firstInput = "";
    private final StringBuilder pauseLog = new StringBuilder();
    private final StringBuilder freezeLog = new StringBuilder();

    //For logging var
    private int loggerTicks = 0;
    private long loggerPausedTime = 0;

    @NotNull
    private TimerStatus status = TimerStatus.NONE;

    private final HashMap<Integer, Integer> moreData = new HashMap<>();

    /**
     * Start the Timer, Trigger when player to join(created) the world
     */
    public static void start() {
        INSTANCE = new InGameTimer();
        INSTANCE.category = SpeedRunOptions.getOption(SpeedRunOptions.TIMER_CATEGORY);
        INSTANCE.setPause(true, INSTANCE.getCategory() == RunCategory.CUSTOM ? TimerStatus.WAITING : TimerStatus.IDLE);
    }

    /**
     * End the Timer, Trigger when player leave
     */
    public static void end() {
        INSTANCE.setStatus(TimerStatus.NONE);
    }

    public static String timeToStringFormat(long time) {
        int seconds = ((int) (time / 1000)) % 60;
        int minutes = ((int) (time / 1000)) / 60;
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, time % 1000);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, time % 1000);
        }
    }

    /**
     * End the Timer, Trigger when Complete Ender Dragon
     */
    public static void complete() {
        InGameTimer timer = INSTANCE;
        if (timer.getStatus() == TimerStatus.COMPLETED) return;

        timer.endTime = System.currentTimeMillis();
        timer.setStatus(TimerStatus.COMPLETED);
        timer.pauseLog.append("Result > IGT ").append(timeToStringFormat(timer.getInGameTime(false)))
                .append(", R-RTA ").append(timeToStringFormat(timer.getRealTimeAttack(true)))
                .append(", RTA ").append(timeToStringFormat(timer.getRealTimeAttack(false)))
                .append(", Counted Ticks: ").append(timer.activateTicks)
                .append(", Total Ticks: ").append(timer.loggerTicks)
                .append(", Rebased RTA Time: ").append(timeToStringFormat(timer.rebaseRealTime))
                .append(", Rebased IGT Time: ").append(timeToStringFormat(timer.rebaseIGTime));

        new Thread(() -> {
            try {
                FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(currentWorldName).toFile(), "igt_log.txt"), timer.firstInput + "\n" + timer.pauseLog, Charsets.UTF_8);
                FileUtils.writeStringToFile(new File(SpeedRunIGT.WORLDS_PATH.resolve(currentWorldName).toFile(), "freeze_log.txt"), timer.freezeLog.toString(), Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        for (Consumer<InGameTimer> onCompleteConsumer : onCompleteConsumers) {
            try {
                onCompleteConsumer.accept(INSTANCE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void leave() {
        if (INSTANCE.getStatus() == TimerStatus.COMPLETED) return;

        INSTANCE.setPause(true, TimerStatus.LEAVE);

        String data = SpeedRunIGT.GSON.toJson(INSTANCE);
        String timerData = Crypto.encrypt(data, "faRQOs2GK5j863ePvCBe5SiZLypm4UOM");
        try {
            FileUtils.writeStringToFile(new File(SpeedRunIGT.TIMER_PATH.toFile(), currentWorldName+".igt"), timerData, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean load(String name) {
        File file = new File(SpeedRunIGT.TIMER_PATH.toFile(), name+".igt");
        if (file.exists()) {
            try {
                String data = Crypto.decrypt(FileUtils.readFileToString(file, StandardCharsets.UTF_8), "faRQOs2GK5j863ePvCBe5SiZLypm4UOM");
                INSTANCE = SpeedRunIGT.GSON.fromJson(data, InGameTimer.class);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }



    public @NotNull RunCategory getCategory() {
        return category;
    }

    public long getTicks() {
        return this.activateTicks;
    }

    public int getMoreData(int key) {
        return moreData.getOrDefault(key, 0);
    }

    public void updateMoreData(int key, int value) {
        moreData.put(key, value);
    }

    public @NotNull TimerStatus getStatus() {
        return status;
    }

    public void setStatus(@NotNull TimerStatus status) {
        if (this.getStatus() == TimerStatus.COMPLETED && status != TimerStatus.NONE) return;
        this.status = status;
    }

    public boolean isStarted() {
        return this.startTime != 0 && this.status != TimerStatus.WAITING;
    }

    public boolean isPaused() {
        return this.getStatus() == TimerStatus.PAUSED || this.getStatus() == TimerStatus.IDLE || this.getStatus() == TimerStatus.LEAVE || this.getStatus() == TimerStatus.WAITING;
    }

    public boolean isPlaying() {
        return !this.isPaused() && this.getStatus() != TimerStatus.COMPLETED;
    }

    private long getEndTime() {
        return this.getStatus() == TimerStatus.COMPLETED ? this.endTime : System.currentTimeMillis();
    }

    public long getStartTime() {
        return this.isStarted() ? startTime : System.currentTimeMillis();
    }

    public long getRealTimeAttack() {
        return getRealTimeAttack(true);
    }

    public long getRealTimeAttack(boolean withRebased) {
        return this.getStatus() == TimerStatus.NONE ? 0 : this.getEndTime() - this.getStartTime() + (withRebased ? rebaseRealTime : 0);
    }

    public long getInGameTime() { return getInGameTime(true); }

    public long getInGameTime(boolean smooth) {
        long ms = System.currentTimeMillis();
        return !isStarted() ? 0 :
                (this.getTicks() * 50L) // Tick Based
                        + Math.min(50, smooth && isPlaying() && this.leastTickTime != 0 ? ms - this.leastTickTime : 0) // More smooth timer in playing
                        - this.rebaseIGTime; // Subtract Rebased time
    }

    public void updateFirstInput() {
        if (firstInput.isEmpty()) {
            firstInput = "First Input: IGT " + timeToStringFormat(getInGameTime()) + ", RTA " + timeToStringFormat(getRealTimeAttack());
        }
    }


    public void tick() {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        if (isPlaying()) {
            this.activateTicks++;
        }
        this.loggerTicks++;

        long currentTime = System.currentTimeMillis();
        long tickDelays = currentTime - leastTickTime;
        boolean isRebasedRTA = false;

        //Rebase time (When a joined world or changed dimension)
        if (leastStartTime != 0 && leastTickTime != 0) {
            rebaseIGTime += MathHelper.clamp((leastStartTime - leastTickTime) * 1.0 / tickDelays, 0, 1) * 50.0;
            leastStartTime = 0;
            throwFPSTick = loggerTicks;
        }
        if (tickDelays < 49 && loggerTicks - throwFPSTick < 100) {
            rebaseRealTime += Math.max(0, 50 - tickDelays);
            isRebasedRTA = true;
        }

        this.leastTickTime = currentTime;

        //Logger
        if (tickDelays != currentTime && Math.abs(50 - tickDelays) > 1) {
            this.freezeLog.append(timeToStringFormat(getInGameTime(false))).append(" IGT, ").append(timeToStringFormat(getRealTimeAttack())).append(" RTA C, ")
                    .append(timeToStringFormat(getRealTimeAttack() - tickDelays)).append(" RTA P, ")
                    .append(tickDelays).append(" Tick delays ms, #").append(loggerTicks).append("(").append(activateTicks).append(") Tick");
            if (this.getStatus() == TimerStatus.IDLE) {
                this.freezeLog.append(", Waiting load or input");
            }
            if (isRebasedRTA && Math.max(0, 50 - tickDelays) > 0) {
                this.freezeLog.append(", Retimed RTA +").append(Math.max(0, 50 - tickDelays)).append("ms");
            }
            this.freezeLog.append("\n");
        }
    }

    public void setPause(boolean isPause) { this.setPause(isPause, TimerStatus.PAUSED); }
    public void setPause(boolean toPause, TimerStatus toStatus) {
        if (this.getStatus() == TimerStatus.COMPLETED) return;

        if (toPause) {
            if (this.getStatus().getPause() <= toStatus.getPause()) {
                if (this.getStatus().getPause() < 1 && this.isStarted()) {
                    loggerPausedTime = getRealTimeAttack();
                    pauseLog.append(timeToStringFormat(getInGameTime())).append(" IGT, ").append(timeToStringFormat(loggerPausedTime)).append(" RTA S, ");
                }
                this.setStatus(toStatus);
            }
        } else {
            if (this.isStarted()) {
                if (isPaused()) {
                    long nowTime = getRealTimeAttack();
                    pauseLog.append(timeToStringFormat(nowTime)).append(" RTA E, ").append(timeToStringFormat(nowTime - loggerPausedTime)).append(" Length (").append(getStatus().getMessage()).append(")\n");
                }
                if (this.getStatus() == TimerStatus.IDLE && loggerTicks != 0) {
                    leastStartTime = System.currentTimeMillis();
                }
            } else {
                startTime = System.currentTimeMillis();
                if (loggerTicks != 0) leastStartTime = startTime;
            }
            this.setStatus(TimerStatus.RUNNING);
        }
    }
}
