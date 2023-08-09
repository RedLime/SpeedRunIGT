package com.redlimerl.speedrunigt.instance;

public enum TimerMode {
    SINGLE_PLAYER(TimerHierarchy.LEADER, GameMode.SINGLE_PLAYER),
    MULTIPLAYER_SERVER(TimerHierarchy.LEADER, GameMode.MULTIPLAYER),
    MULTIPLAYER_CLIENT(TimerHierarchy.FOLLOWER, GameMode.MULTIPLAYER);

    public final TimerHierarchy timerHierarchy;
    public final GameMode gameMode;

    TimerMode(TimerHierarchy timerHierarchy, GameMode gameMode) {
        this.timerHierarchy = timerHierarchy;
        this.gameMode = gameMode;
    }
}
