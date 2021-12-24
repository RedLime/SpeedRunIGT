package com.redlimerl.speedrunigt.timer;

/**
 * Status of the timer.
 */
public enum TimerStatus {
    /**
     * Nothing, When the player is in the world select menu.
     */
    NONE("", -1),

    /**
     * When the player created the world or changed dimension.
     * Waits for the first input of the player's interaction.
     * It will be not change by {@link TimerStatus#PAUSED}.
     */
    IDLE("Paused by change dimension", 2),

    /**
     * When the player has stopped the game itself.
     * Like, Esc/F3+Es.
     */
    PAUSED("Paused by player", 1),

    /**
     * When the In-game-time(IGT) is running.
     */
    RUNNING("Running", 0),

    /**
     * When the player kills the ender dragon and sees the credit screen.
     * This status doesn't change until the player creates a new world.
     */
    COMPLETED("Completed the category", 1),

    /**
     * When the player leaves the world for some reason.
     * If join the same world again, it will change to {@link TimerStatus#RUNNING}.
     */
    LEAVE_LEGACY("Leave the world by player", 1);

    private final String message;
    private final int pause;

    TimerStatus(String message, int pause) {
        this.message = message;
        this.pause = pause;
    }

    public String getMessage() {
        return message;
    }

    public int getPause() {
        return pause;
    }
}
