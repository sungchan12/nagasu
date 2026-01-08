package com.mymedia.streamer.utils

fun toSlug(title: String): String {
    return title
        .trim()
        .replace(Regex("\\s+"), "_")
        .replace(Regex("[^a-zA-Z0-9가-힣_-]"), "")
}
