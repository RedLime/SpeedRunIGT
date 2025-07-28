package com.redlimerl.speedrunigt.utils;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ResourcesHelper {
    private static final Path root;

    static {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        List<Path> paths = FabricLoader.getInstance().getModContainer("speedrunigt").get().getRootPaths();
        if (paths.size() != 1) {
            throw new IllegalStateException("unknown jar type");
        }
        root = paths.get(0);
    }

    public static String[] getResourceChildren(String folder) throws IOException, URISyntaxException {

        try (Stream<Path> children = Files.list(root.resolve(folder))) {
            return children.map(inner -> inner.subpath(inner.getNameCount() - 2, inner.getNameCount())).map(Path::toString).toArray(String[]::new);
        }
    }

    public static InputStream toStream(String resource) {
        Path path = root.resolve(resource);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
