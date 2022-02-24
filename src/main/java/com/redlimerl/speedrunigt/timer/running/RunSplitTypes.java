package com.redlimerl.speedrunigt.timer.running;

public class RunSplitTypes {

    public static RunSplitType ERROR_SPLIT = new RunSplitType("error", "Not found split :(");

    public static RunSplitType ENTER_NETHER = new RunSplitType("ENTER_NETHER", "Enter the Nether");
    public static RunSplitType ENTER_END = new RunSplitType("ENTER_END", "Enter the End");
    public static RunSplitType ENTER_STRONG_HOLD = new RunSplitType("ENTER_STRONG_HOLD", "Found the Stronghold");
    public static RunSplitType ENTER_FORTRESS = new RunSplitType("ENTER_FORTRESS", "Found the Fortress");
    public static RunSplitType ENTER_BASTION = new RunSplitType("ENTER_BASTION", "Found the bastion");
    public static RunSplitType COMPLETE = new RunSplitType("COMPLETE", "speedrunigt.split.complete_run");

}
