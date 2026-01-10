package com.mymedia.streamer.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mymedia.streamer.dto.metadata.CollectionMetadata
import java.io.File

private val objectMapper = jacksonObjectMapper()

/**
 * 디렉토리가 존재하지 않으면 생성한다.
 * @return 디렉토리가 존재하거나 성공적으로 생성되면 true
 */
fun File.ensureExists(): Boolean {
    return this.exists() || this.mkdirs()
}

val IMAGE_EXTENSION = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")
fun File.isImageFile(): Boolean {
    return this.isFile && this.extension.lowercase() in IMAGE_EXTENSION
}

val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mkv", "mov", "webm")
fun File.isVideoFile(): Boolean {
    return this.isFile && this.extension.lowercase() in VIDEO_EXTENSIONS
}

fun File.countImageFiles(): Int {
    return this.walkTopDown().count {it.isImageFile() }
}

fun File.countVideoFiles(): Int {
    return this.walkTopDown().count {it.isVideoFile() }
}

fun File.getMetaData(): CollectionMetadata? {
    val metadataFile = File(this, "metadata.json")
    if (!metadataFile.exists()) return null
    return objectMapper.readValue<CollectionMetadata>(metadataFile)
}

fun File.saveMetaData(metadata: CollectionMetadata) {
    val metadataFile = File(this, "metadata.json")
    objectMapper.writeValue(metadataFile, metadata)
}