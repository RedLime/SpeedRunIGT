package com.redlimerl.speedrunigt.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FontUtils {
    public static void addFont(HashMap<Identifier, List<Font>> map, File file, File configFile) {
        FileInputStream fileInputStream = null;
        STBTTFontinfo sTBTTFontinfo = null;
        ByteBuffer byteBuffer = null;
        Throwable throwable = null;

        try {
            fileInputStream = new FileInputStream(file);
            sTBTTFontinfo = STBTTFontinfo.malloc();
            byteBuffer = TextureUtil.readAllToByteBuffer(fileInputStream);
            byteBuffer.flip();
            if (!STBTruetype.stbtt_InitFont(sTBTTFontinfo, byteBuffer)) {
                return;
            }

            Identifier fontIdentifier = new Identifier(SpeedRunIGT.MOD_ID, file.getName().toLowerCase(Locale.ROOT).replace(".ttf", "").replaceAll(" ", "_").replaceAll("[^a-z0-9/._-]", ""));
            ArrayList<Font> fontArrayList = new ArrayList<>();

            if (configFile != null && configFile.exists()) {
                FontConfigure fontConfigure = FontConfigure.fromJson(FileUtils.readFileToString(configFile, StandardCharsets.UTF_8));
                fontArrayList.add(new TrueTypeFont(byteBuffer, sTBTTFontinfo,
                        fontConfigure.size, fontConfigure.oversample, fontConfigure.shift[0], fontConfigure.shift[1], fontConfigure.skip));
            } else {
                fontArrayList.add(new TrueTypeFont(byteBuffer, sTBTTFontinfo, 11f, 6f, 0, 0, ""));
            }

            fontArrayList.add(new BlankFont());

            map.put(fontIdentifier, fontArrayList);
        } catch (FileNotFoundException e) {
            if (sTBTTFontinfo != null) sTBTTFontinfo.free();
            MemoryUtil.memFree(byteBuffer);
        } catch (IOException throwable1) {
            throwable = throwable1;
        } finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
            } catch (IOException e) {
                if (throwable != null) throwable.addSuppressed(e);
            }
        }
    }
}
