package com.mymedia.nagasu.repository;

import com.mymedia.nagasu.utils.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VideoRepository {

    private VideoRepository() {}

    public static List<String> getVideoCollection(File dir) {
        var files = dir.listFiles();
        if (files == null) return Collections.emptyList();
        return Arrays.stream(files)
                .filter(File::isDirectory)
                .map(File::getName)
                .toList();
    }

    public static String getVideoFileName(File dir) {
        if (!dir.isDirectory()) return null;
        var files = dir.listFiles();
        if (files == null) return null;
        return Arrays.stream(files)
                .filter(FileUtils::isVideoFile)
                .map(File::getName)
                .findFirst()
                .orElse(null);
    }

    public static String getSubtitleFileName(File dir) {
        if (!dir.isDirectory()) return null;
        var files = dir.listFiles();
        if (files == null) return null;
        // Prefer .vtt
        var vtt = Arrays.stream(files)
                .filter(f -> FileUtils.getExtension(f).equalsIgnoreCase("vtt"))
                .map(File::getName)
                .findFirst();
        return vtt.orElseGet(() -> Arrays.stream(files)
                .filter(FileUtils::isSubtitleFile)
                .map(File::getName)
                .findFirst()
                .orElse(null));
        // Fallback to any subtitle file
    }
}