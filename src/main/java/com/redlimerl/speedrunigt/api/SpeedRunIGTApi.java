package com.redlimerl.speedrunigt.api;


import java.util.ArrayList;
import java.util.Collection;

public interface SpeedRunIGTApi {

    default OptionButtonFactory createOptionButton() {
        return null;
    }

    default Collection<OptionButtonFactory> createOptionButtons() {
        return new ArrayList<>();
    }

    default void registerCategory() {
    }
}
