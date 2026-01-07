package com.mymedia.streamer.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mymedia.streamer.dto.metadata.ImageMetadata
import com.mymedia.streamer.utils.isImageFile
import java.io.File

fun File.getCollectionDirs(): List<File> {
    return this.listFiles()?.filter { it.isDirectory } ?: emptyList()
}

fun File.getImageFiles(): List<File> {
    return this.walkTopDown().filter { it.isImageFile()}.sortedBy { it.name }.toList()
}

fun File.getThumbnailImageFile(): File? {
    if (!this.isDirectory) return null
    val thumbnailFile = this.listFiles()
        ?.find { it.nameWithoutExtension.lowercase() == "thumbnail" && it.isImageFile() }
    if (thumbnailFile != null) return thumbnailFile
    return this.listFiles()
        ?.filter { it.isImageFile() }
        ?.minByOrNull { it.name }
}
