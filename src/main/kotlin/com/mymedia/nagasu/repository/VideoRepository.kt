package com.mymedia.nagasu.repository

import com.mymedia.nagasu.utils.isVideoFile
import com.mymedia.nagasu.utils.isSubtitleFile
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
    return this.listFiles()?.find { it.extension.equals("vtt", true) }?.name
        ?: this.listFiles()?.firstOrNull{ it.isSubtitleFile() }?.name
}