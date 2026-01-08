package com.mymedia.streamer.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mymedia.streamer.dto.metadata.ImageMetadata
import com.mymedia.streamer.utils.isImageFile
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

private val objectMapper = jacksonObjectMapper()

fun File.getCollectionDirs(): List<File> {
    return this.listFiles()?.filter { it.isDirectory } ?: emptyList()
}

fun File.getImageFileNames(): List<String> {
    return this.walkTopDown().filter { it.isImageFile() }.map { it.name }.sorted().toList()
}

fun File.getThumbnailFileName(): String? {
    if (!this.isDirectory) return null
    val thumbnailFile = this.listFiles()
        ?.find { it.nameWithoutExtension.lowercase() == "thumbnail" && it.isImageFile() }
    if (thumbnailFile != null) return thumbnailFile.name
    return this.listFiles()
        ?.filter { it.isImageFile() }
        ?.minByOrNull { it.name }
        ?.name
}

fun File.getMetaData(): ImageMetadata? {
    val metadataFile = File(this, "metadata.json")
    if (!metadataFile.exists()) return null
    return objectMapper.readValue<ImageMetadata>(metadataFile)
}

fun File.saveMetaData(metadata: ImageMetadata) {
    val metadataFile = File(this, "metadata.json")
    objectMapper.writeValue(metadataFile, metadata)
}