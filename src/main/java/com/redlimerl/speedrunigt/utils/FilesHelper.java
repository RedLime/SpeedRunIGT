package com.redlimerl.speedrunigt.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FilesHelper {
    public static void recursiveCopy(Path oldPath, Path newPath) throws IOException {
        Files.createDirectories(oldPath);
        Files.walkFileTree(oldPath, new SimpleFileVisitor<Path>() {
            @Override
            public @NotNull FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.copy(file, newPath.resolve(oldPath.relativize(file)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult preVisitDirectory(final Path dir, final @NotNull BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(newPath.resolve(oldPath.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
