package com.redlimerl.speedrunigt.instance;

import com.redlimerl.speedrunigt.events.EventRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Instance {
    public static final ExecutorService saveManagerThread = Executors.newSingleThreadExecutor();
    private WorldFolder worldFolder;
    public EventRepository eventRepository;

    // TODO: function to get current list of events from world folder event repo if world host, otherwise get from network events memory
}
