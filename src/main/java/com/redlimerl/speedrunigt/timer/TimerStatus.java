package com.redlimerl.speedrunigt.timer;

/**
 * Status of the timer.
 */
public enum TimerStatus {
    /**
     * Nothing, When the player is in the world select menu.
     * But, It's different to {@link TimerStatus#LEAVE}
     */
    NONE,

    /**
     * When the player created the world or changed dimension.
     * Waits for the first input of the player's interaction.
     * It will be not change by {@link TimerStatus#PAUSED}.
     */
    IDLE,

    /**
     * When the player has stopped the game itself.
     * Like, Esc/F3+Es.
     */
    PAUSED,

    /**
     * When the In-game-time(IGT) is running.
     */
    RUNNING,

    /**
     * When the player kills the ender dragon and sees the credit screen.
     * This status doesn't change until the player creates a new world.
     */
    COMPLETED,

    /**
     * When the player leaves the world for some reason.
     * If join the same world again, it will change to {@link TimerStatus#RUNNING}.
     */
    LEAVE
}
