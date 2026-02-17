package com.mymedia.nagasu.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mymedia.nagasu.dto.metadata.CollectionMetadata
import org.slf4j.LoggerFactory
import java.io.File

private val objectMapper = jacksonObjectMapper()

/**
 * 디렉토리가 존재하지 않으면 생성한다.
 * @return 디렉토리가 존재하거나 성공적으로 생성되면 true
 */
fun File.ensureExists(): Boolean {
    return this.exists() || this.mkdirs()
}

val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")
fun File.isImageFile(): Boolean {
    return this.isFile && this.extension.lowercase() in IMAGE_EXTENSIONS
}

val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mkv", "mov", "webm")
fun File.isVideoFile(): Boolean {
    return this.isFile && this.extension.lowercase() in VIDEO_EXTENSIONS
}

val SUBTITLE_EXTENSIONS = setOf("vtt", "srt")
fun File.isSubtitleFile(): Boolean {
    return this.isFile && this.extension.lowercase() in SUBTITLE_EXTENSIONS
}

fun File.countImageFiles(): Int {
    return this.walkTopDown().count { it.isImageFile() }
}

fun File.countVideoFiles(): Int {
    return this.walkTopDown().count { it.isVideoFile() }
}

fun File.getMetaData(): CollectionMetadata? {
    val metadataFile = File(this, "metadata.json")
    if (!metadataFile.exists()) return null
    return try {
        objectMapper.readValue<CollectionMetadata>(metadataFile)
    } catch (e: Exception) {
        LoggerFactory
            .getLogger("FileUtils")
            .warn("metadata parsing incomplete: ${this.absolutePath}/metadata.json - ${e.message}")
        null
    }
}

fun File.saveMetaData(metadata: CollectionMetadata) {
    val metadataFile = File(this, "metadata.json")
    objectMapper.writeValue(metadataFile, metadata)
}

fun File.incrementViewCount() {
    val metadata = this.getMetaData() ?: return
    this.saveMetaData(metadata.copy(viewCount = metadata.viewCount + 1))
}