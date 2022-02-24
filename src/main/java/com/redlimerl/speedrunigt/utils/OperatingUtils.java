package com.redlimerl.speedrunigt.utils;

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
}
