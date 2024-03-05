package com.redlimerl.speedrunigt.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;

class NbtCompoundParser extends NbtParser {
    protected ArrayList<NbtParser> parsers = new ArrayList<>();

    public NbtCompoundParser(String string) {
        this.key = string;
    }

    @Override
    public NbtElement parse() {
        NbtCompound var1 = new NbtCompound();

        for (NbtParser var3 : this.parsers) {
            var1.put(var3.key, var3.parse());
        }

        return var1;
    }
}
