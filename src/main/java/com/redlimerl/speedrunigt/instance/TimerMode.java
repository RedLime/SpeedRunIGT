package com.redlimerl.speedrunigt.instance;

public enum TimerMode {
    SINGLE_PLAYER(TimerRelationship.LEADER, GameMode.SINGLE_PLAYER),
    MULTIPLAYER_SERVER(TimerRelationship.LEADER, GameMode.MULTIPLAYER),
    MULTIPLAYER_CLIENT(TimerRelationship.FOLLOWER, GameMode.MULTIPLAYER);

    public final TimerRelationship timerRelationship;
    public final GameMode gameMode;

    TimerMode(TimerRelationship timerRelationship, GameMode gameMode) {
        this.timerRelationship = timerRelationship;
        this.gameMode = gameMode;
    }
}
