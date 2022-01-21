package com.redlimerl.speedrunigt.utils;

import com.mojang.blaze3d.platform.TextureUtil;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
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
        ByteBuffer byteBuffer = null;
        Throwable throwable = null;

        try {
            fileInputStream = new FileInputStream(file);
            byteBuffer = TextureUtil.readResource(fileInputStream);
            byteBuffer.flip();

            Identifier fontIdentifier = new Identifier(SpeedRunIGT.MOD_ID, file.getName().toLowerCase(Locale.ROOT).replace(".ttf", "").replaceAll(" ", "_").replaceAll("[^a-z0-9/._-]", ""));
            ArrayList<Font> fontArrayList = new ArrayList<>();

            FontConfigure fontConfigure;
            if (configFile != null && configFile.exists()) {
                fontConfigure = FontConfigure.fromJson(FileUtils.readFileToString(configFile, StandardCharsets.UTF_8));
            } else {
                fontConfigure = FontConfigure.create();
            }
            fontArrayList.add(new TrueTypeFont(TrueTypeFont.getSTBTTFontInfo(byteBuffer), fontConfigure.size, fontConfigure.oversample, fontConfigure.shift[0], fontConfigure.shift[1], fontConfigure.skip));
            SpeedRunIGT.FONT_MAPS.put(fontIdentifier, new FontIdentifier(file, fontIdentifier, fontConfigure));

            fontArrayList.add(new BlankFont());

            map.put(fontIdentifier, fontArrayList);
        } catch (FileNotFoundException e) {
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
