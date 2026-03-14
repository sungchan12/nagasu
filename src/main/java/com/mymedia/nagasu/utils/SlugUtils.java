package com.mymedia.nagasu.utils;

public class SlugUtils {

    private SlugUtils() {}

    public static String toSlug(String title) {
        return title.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9가-힣_-]", "");
    }

    public static void validateCollectionId(String collectionId) {
        if (!collectionId.matches("^[a-zA-Z0-9가-힣_-]+$")) {
            throw new IllegalArgumentException(
                    "Invalid collection ID: '" + collectionId + "'. Only alphanumeric, Korean, hyphen(-), and underscore(_) characters are allowed."
            );
        }
    }
}