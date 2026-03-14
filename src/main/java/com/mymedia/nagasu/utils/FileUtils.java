package com.mymedia.nagasu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymedia.nagasu.dto.metadata.CollectionMetadata;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FileUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "svg");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mkv", "mov", "webm");
    private static final Set<String> SUBTITLE_EXTENSIONS = Set.of("vtt", "srt");

    private FileUtils() {}

    public static boolean ensureExists(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    public static boolean isImageFile(File file) {
        return file.isFile() && IMAGE_EXTENSIONS.contains(getExtension(file).toLowerCase());
    }

    public static boolean isVideoFile(File file) {
        return file.isFile() && VIDEO_EXTENSIONS.contains(getExtension(file).toLowerCase());
    }

    public static boolean isSubtitleFile(File file) {
        return file.isFile() && SUBTITLE_EXTENSIONS.contains(getExtension(file).toLowerCase());
    }

    public static String getExtension(File file) {
        var name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : "";
    }

    public static String getNameWithoutExtension(File file) {
        var name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }

    public static CollectionMetadata getMetaData(File dir) {
        var metadataFile = new File(dir, "metadata.json");
        if (!metadataFile.exists()) return null;
        try {
            return objectMapper.readValue(metadataFile, CollectionMetadata.class);
        } catch (IOException e) {
            logger.warn("metadata parsing incomplete: {}/metadata.json - {}", dir.getAbsolutePath(), e.getMessage());
            return null;
        }
    }

    public static void saveMetaData(File dir, CollectionMetadata metadata) {
        var metadataFile = new File(dir, "metadata.json");
        try {
            objectMapper.writeValue(metadataFile, metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save metadata: " + dir.getAbsolutePath(), e);
        }
    }

    public static void incrementViewCount(File dir) {
        var metadata = getMetaData(dir);
        if (metadata == null) return;
        saveMetaData(dir, new CollectionMetadata(
                metadata.title(),
                metadata.artist(),
                metadata.tags(),
                metadata.description(),
                metadata.viewCount() + 1
        ));
    }

    /**
     * Prevents Path Traversal by verifying the canonicalized path stays within basePath.
     */
    public static File validatePath(File basePath, String userInput) {
        try {
            var resolved = new File(basePath, userInput).getCanonicalFile();
            var base = basePath.getCanonicalFile();
            if (!resolved.getPath().startsWith(base.getPath() + File.separator) && !resolved.equals(base)) {
                throw new IllegalArgumentException("Invalid path: access denied");
            }
            return resolved;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid path: " + userInput, e);
        }
    }
}