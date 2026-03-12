package com.mymedia.nagasu.utils

fun toSlug(title: String): String {
    return title
        .trim()
        .replace(Regex("\\s+"), "_")
        .replace(Regex("[^a-zA-Z0-9가-힣_-]"), "")
}

fun validateCollectionId(collectionId: String) {
    require(collectionId.matches(Regex("^[a-zA-Z0-9가-힣_-]+$")))
}