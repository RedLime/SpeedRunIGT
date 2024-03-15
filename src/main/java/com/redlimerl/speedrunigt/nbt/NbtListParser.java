package com.redlimerl.speedrunigt.nbt;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;

class NbtListParser extends NbtParser {
    protected ArrayList<NbtParser> parsers = new ArrayList<>();

    public NbtListParser(String string) {
        this.key = string;
    }

    @Override
    public NbtElement parse() {
        NbtList var1 = new NbtList();

        for (NbtParser var3 : this.parsers) {
            var1.method_1217(var3.parse());
        }

        return var1;
    }
}
