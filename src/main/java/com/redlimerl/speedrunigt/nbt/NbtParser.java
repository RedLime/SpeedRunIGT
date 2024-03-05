package com.redlimerl.speedrunigt.nbt;

import net.minecraft.nbt.NbtElement;

abstract class NbtParser {
    protected String key;

    public abstract NbtElement parse();
}
