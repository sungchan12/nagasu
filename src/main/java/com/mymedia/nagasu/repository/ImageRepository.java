package com.mymedia.nagasu.repository;

import com.mymedia.nagasu.utils.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ImageRepository {

    private ImageRepository() {}

    public static List<String> getCollectionDirs(File dir) {
        var files = dir.listFiles();
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .map(File::getName)
                .toList();
    }

    public static List<String> getImageFileNames(File dir) {
        return walkFiles(dir)
                .filter(FileUtils::isImageFile)
                .map(File::getName)
                .sorted()
                .toList();
    }

    public static String getThumbnailFileName(File dir) {
        if (!dir.isDirectory()) return null;
        var files = dir.listFiles();
        if (files == null) return null;

        // First look for a file named "thumbnail"
        for (var file : files) {
            if (FileUtils.getNameWithoutExtension(file).equalsIgnoreCase("thumbnail") && FileUtils.isImageFile(file)) {
                return file.getName();
            }
        }
        // Fallback: first image file by name
        return Arrays.stream(files)
                .filter(FileUtils::isImageFile)
                .map(File::getName)
                .min(String::compareTo)
                .orElse(null);
    }

    private static Stream<File> walkFiles(File dir) {
        var files = dir.listFiles();
        if (files == null) return Stream.empty();
        return Arrays.stream(files).flatMap(f -> {
            if (f.isDirectory()) return walkFiles(f);
            return Stream.of(f);
        });
    }
}