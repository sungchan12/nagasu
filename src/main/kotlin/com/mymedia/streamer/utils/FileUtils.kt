package com.mymedia.streamer.utils

import java.io.File

/**
 * 디렉토리가 존재하지 않으면 생성한다.
 * @return 디렉토리가 존재하거나 성공적으로 생성되면 true
 */
fun File.ensureExists(): Boolean {
    return this.exists() || this.mkdirs()
}