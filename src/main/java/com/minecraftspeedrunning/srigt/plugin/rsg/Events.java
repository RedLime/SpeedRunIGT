package com.minecraftspeedrunning.srigt.plugin.rsg;

import com.minecraftspeedrunning.srigt.common.events.EventFactory;

public class Events {

    private static final String RSG_SOURCE = "rsg";

    public static final EventFactory ENTER_NETHER = new EventFactory(RSG_SOURCE, "enter_nether");
    public static final EventFactory ENTER_BASTION = new EventFactory(RSG_SOURCE, "enter_bastion");
    public static final EventFactory ENTER_FORTRESS = new EventFactory(RSG_SOURCE, "enter_fortress");
    public static final EventFactory ENTER_STRONGHOLD = new EventFactory(RSG_SOURCE, "enter_stronghold");
    public static final EventFactory ENTER_END = new EventFactory(RSG_SOURCE, "enter_end");
    public static final EventFactory DRAGON_DEATH = new EventFactory(RSG_SOURCE, "dragon_death");
    public static final EventFactory CREDITS = new EventFactory(RSG_SOURCE, "credits");
}
