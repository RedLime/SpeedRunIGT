package com.redlimerl.speedrunigt.events;



public class Events {
    private static final String SRIGT_SOURCE = "srigt";

    public static final EventFactory WORLD_LOADED = new EventFactory(SRIGT_SOURCE, "world_loaded");
    public static final EventFactory PLAYER_JOIN = new EventFactory(SRIGT_SOURCE, "player_join");
    public static final EventFactory FIRST_INPUT = new EventFactory(SRIGT_SOURCE, "first_input");

    public static final EventFactory GAME_PAUSE = new EventFactory(SRIGT_SOURCE, "game_pause");
    public static final EventFactory GAME_UNPAUSE = new EventFactory(SRIGT_SOURCE, "game_unpause");
    public static final EventFactory OPEN_LAN = new EventFactory(SRIGT_SOURCE, "open_lan");
    public static final EventFactory ENTER_NETHER = new EventFactory(SRIGT_SOURCE, "enter_nether");
    public static final EventFactory ENTER_BASTION = new EventFactory(SRIGT_SOURCE, "enter_bastion");
    public static final EventFactory ENTER_FORTRESS = new EventFactory(SRIGT_SOURCE, "enter_fortress");
    public static final EventFactory ENTER_STRONGHOLD = new EventFactory(SRIGT_SOURCE, "enter_stronghold");
    public static final EventFactory ENTER_END = new EventFactory(SRIGT_SOURCE, "enter_end");
    public static final EventFactory DRAGON_DEATH = new EventFactory(SRIGT_SOURCE, "dragon_death");
    public static final EventFactory CREDITS = new EventFactory(SRIGT_SOURCE, "credits");
}
