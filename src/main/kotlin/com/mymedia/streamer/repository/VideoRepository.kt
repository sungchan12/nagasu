package com.mymedia.streamer.repository

import com.mymedia.streamer.dto.VideoCollectionResponse
import java.io.File

fun File.getVideoCollection(): List<File> {
    return this.listFiles()?.filter { it.isDirectory } ?: emptyList()
}