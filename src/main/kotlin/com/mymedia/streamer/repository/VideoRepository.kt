package com.mymedia.streamer.repository

import com.mymedia.streamer.utils.isVideoFile
import com.mymedia.streamer.utils.isSubtitleFile
import java.io.File

fun File.getVideoCollection(): List<String> {
    return this.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
}

fun File.getVideoFileName(): String? {
    if (!this.isDirectory) return null
    return this.listFiles()?.firstOrNull { it.isVideoFile() }?.name
}

fun File.getSubtitleFileName(): String? {
    if (!this.isDirectory) return null
    return this.listFiles()?.firstOrNull { it.isSubtitleFile() }?.name
}