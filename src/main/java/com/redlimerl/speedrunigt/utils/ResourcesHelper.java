package com.redlimerl.speedrunigt.utils;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourcesHelper {
    public static String[] getResourceChildren(String path) throws IOException, URISyntaxException {
        File jarFile = new File(ResourcesHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        ArrayList<String> list = Lists.newArrayList();

        if (jarFile.isFile()) {
            ZipInputStream zip = new ZipInputStream(ResourcesHelper.class.getProtectionDomain().getCodeSource().getLocation().openStream());
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) { break; }
                String name = e.getName();
                if (name.startsWith(path)) {
                    String[] fileName = name.split("/");
                    String file = fileName[fileName.length - 1];
                    if (!file.isEmpty() && fileName.length > 1) {
                        list.add("/" + path + "/" + file);
                    }
                }
            }
        } else {
            URL url = ResourcesHelper.class.getResource("/" + path);
            if (url != null) {
                File apps = new File(url.toURI());
                for (File app : Objects.requireNonNull(apps.listFiles())) {
                    list.add("/" + path + "/" + app.getName());
                }
            }
        }
        return list.toArray(new String[0]);
    }

    public static InputStream toStream(String resource) {
        return ResourcesHelper.class.getResourceAsStream(resource);
    }
}
