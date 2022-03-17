package com.redlimerl.speedrunigt.utils;

import net.minecraft.util.Util;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class OperatingUtils {
    public static void setUrl(String url) {
        try {
            Class<?> class_ = Class.forName("java.awt.Desktop");
            Object object = class_.getMethod("getDesktop").invoke(null);
            class_.getMethod("browse", URI.class).invoke(object, new URI(url));
        } catch (Throwable var4) {
            var4.printStackTrace();
        }
    }


    public static void setFile(File file) {
        String string = file.getAbsolutePath();
        if (Util.getOperatingSystem() == Util.OperatingSystem.MACOS) {
            try {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", string});
                return;
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        } else if (Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS) {
            String string2 = String.format("cmd.exe /C start \"Open file\" \"%s\"", string);

            try {
                Runtime.getRuntime().exec(string2);
                return;
            } catch (IOException var6) {
                var6.printStackTrace();
            }
        }

        boolean bl = false;

        try {
            Class<?> class_ = Class.forName("java.awt.Desktop");
            Object object = class_.getMethod("getDesktop").invoke(null);
            class_.getMethod("browse", URI.class).invoke(object, file.toURI());
        } catch (Throwable var5) {
            var5.printStackTrace();
            bl = true;
        }

        if (bl) {
            Sys.openURL("file://" + string);
        }

    }
}
