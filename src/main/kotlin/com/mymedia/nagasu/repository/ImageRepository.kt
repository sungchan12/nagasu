package com.mymedia.nagasu.repository

import com.mymedia.nagasu.utils.isImageFile
import java.io.File

fun File.getCollectionDirs(): List<String> {
    return this.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()
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