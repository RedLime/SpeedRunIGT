package com.redlimerl.speedrunigt.utils;

import net.minecraft.util.Identifier;

import java.io.File;

public class FontIdentifier {
    private final File file;
    private final Identifier identifier;
    private final FontConfigure fontConfigure;

    public FontIdentifier(File file, Identifier identifier, FontConfigure fontConfigure) {
        this.file = file;
        this.identifier = identifier;
        this.fontConfigure = fontConfigure;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public FontConfigure getFontConfigure() {
        return fontConfigure;
    }

    public File getFile() {
        return file;
    }
}
