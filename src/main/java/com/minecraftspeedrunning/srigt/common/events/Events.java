package com.minecraftspeedrunning.srigt.common.events;


public class Events {
    private static final String SRIGT_SOURCE = "common";

    public static final EventFactory WORLD_LOADED = new EventFactory(SRIGT_SOURCE, "world_loaded");
    public static final EventFactory PLAYER_JOIN = new EventFactory(SRIGT_SOURCE, "player_join");
    public static final EventFactory FIRST_INPUT = new EventFactory(SRIGT_SOURCE, "first_input");
    public static final EventFactory GAME_PAUSE = new EventFactory(SRIGT_SOURCE, "game_pause");
    public static final EventFactory GAME_UNPAUSE = new EventFactory(SRIGT_SOURCE, "game_unpause");
    public static final EventFactory OPEN_LAN = new EventFactory(SRIGT_SOURCE, "open_lan");
}
