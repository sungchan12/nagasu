package com.mymedia.streamer.utils

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 비디오 파일에서 썸네일을 추출한다.
 * @param videoFile 비디오 파일
 * @param outputFile 썸네일 출력 파일 (jpg)
 * @param timeSeconds 추출할 시간 (초), 기본값 1초
 * @return 성공 여부
 */
fun extractThumbnail(videoFile: File, outputFile: File, timeSeconds: Int = 1): Boolean {
    if (!videoFile.exists()) return false

    val command = listOf(
        "ffmpeg",
        "-y",                          // 덮어쓰기
        "-ss", timeSeconds.toString(), // 시간 위치
        "-i", videoFile.absolutePath,  // 입력 파일
        "-vframes", "1",               // 1프레임만
        "-q:v", "2",                   // 품질 (2가 높은 품질)
        outputFile.absolutePath        // 출력 파일
    )

    return try {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        completed && process.exitValue() == 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * 비디오 파일에서 썸네일을 추출하여 같은 폴더에 thumbnail.jpg로 저장한다.
 * @param videoFile 비디오 파일
 * @param timeSeconds 추출할 시간 (초), 기본값 1초
 * @return 생성된 썸네일 파일, 실패 시 null
 */
fun extractThumbnailToFolder(videoFile: File, timeSeconds: Int = 1): File? {
    val thumbnailFile = File(videoFile.parentFile, "thumbnail.jpg")
    return if (extractThumbnail(videoFile, thumbnailFile, timeSeconds)) {
        thumbnailFile
    } else {
        null
    }
}
