package com.redlimerl.speedrunigt.utils;

import com.mojang.blaze3d.platform.TextureUtil;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import net.minecraft.client.font.BlankFont;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FontUtils {
    public static void addFont(HashMap<Identifier, List<Font>> map, File file, File configFile) {
        FileInputStream fileInputStream = null;
        FT_Face fT_Face = null;
        ByteBuffer byteBuffer = null;
        Throwable throwable = null;

        try {
            fileInputStream = new FileInputStream(file);
            byteBuffer = TextureUtil.readResource(fileInputStream);

            Identifier fontIdentifier = Identifier.of(SpeedRunIGT.MOD_ID, file.getName().toLowerCase(Locale.ROOT).replace(".ttf", "").replaceAll(" ", "_").replaceAll("[^a-z0-9/._-]", ""));
            ArrayList<Font> fontArrayList = new ArrayList<>();

            FontConfigure fontConfigure;
            if (configFile != null && configFile.exists()) {
                fontConfigure = FontConfigure.fromJson(FileUtils.readFileToString(configFile, StandardCharsets.UTF_8));
            } else {
                fontConfigure = FontConfigure.create();
            }

            synchronized (FreeTypeUtil.LOCK) {
                try (MemoryStack memoryStack = MemoryStack.stackPush()) {
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.checkFatalError(FreeType.FT_New_Memory_Face(FreeTypeUtil.initialize(), byteBuffer, 0L, pointerBuffer), "Initializing font face");
                    fT_Face = FT_Face.create(pointerBuffer.get());
                }

                String string = FreeType.FT_Get_Font_Format(fT_Face);
                if (!"TrueType".equals(string)) {
                    throw new IOException("Font is not in TTF format, was " + string);
                }

                FreeTypeUtil.checkFatalError(FreeType.FT_Select_Charmap(fT_Face, FreeType.FT_ENCODING_UNICODE), "Find unicode charmap");
                fontArrayList.add(new TrueTypeFont(byteBuffer, fT_Face, fontConfigure.size, fontConfigure.oversample, fontConfigure.shift[0], fontConfigure.shift[1], fontConfigure.skip));
            }

            SpeedRunIGT.FONT_MAPS.put(fontIdentifier, new FontIdentifier(file, fontIdentifier, fontConfigure));

            fontArrayList.add(new BlankFont());

            map.put(fontIdentifier, fontArrayList);
        } catch (FileNotFoundException e) {
            if (fT_Face != null) fT_Face.free();
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

    public static void copyDefaultFonts() {
        copyResourceToFont("calibri_bold.ttf");
        copyResourceToFont("calibri_bold.json");
    }

    private static void copyResourceToFont(String fileName) {
        File fontFile = SpeedRunIGT.FONT_PATH.resolve(fileName).toFile();
        if (!fontFile.exists()) {
            InputStream fontInput = FontUtils.class.getResourceAsStream("/font/"+fileName);
            if (fontInput == null) return;
            try {
                FileOutputStream output = new FileOutputStream(fontFile);

                byte[] buf = new byte[1024];

                int readData;
                while ((readData = fontInput.read(buf)) > 0) {
                    output.write(buf, 0, readData);
                }

                fontInput.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
