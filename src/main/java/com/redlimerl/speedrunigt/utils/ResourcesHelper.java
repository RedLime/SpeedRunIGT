package com.redlimerl.speedrunigt.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.stream.Stream;

public class ResourcesHelper {
    public static String[] getResourceChildren(String folder) throws IOException, URISyntaxException {
        URL parent = ResourcesHelper.class.getResource("/" + folder);
        if (parent == null) {
            throw new FileNotFoundException(folder);
        }
        Path path;
        if (parent.getProtocol().equals("jar")) {
            // someone already opened the jar, probably fabric loader
            FileSystem fs = FileSystems.getFileSystem(parent.toURI());
            if (fs == null) {
                throw new FileSystemNotFoundException(parent.toURI().toString());
            }
            path = fs.getPath("/" + folder);
        } else {
            path = Paths.get(parent.toURI());
        }
        try (Stream<Path> children = Files.list(path)) {
            return children.map(inner -> inner.subpath(inner.getNameCount() - 2, inner.getNameCount())).map(Path::toString).toArray(String[]::new);
        }
    }

    public static InputStream toStream(String resource) {
        return ResourcesHelper.class.getResourceAsStream(resource);
    }
}
