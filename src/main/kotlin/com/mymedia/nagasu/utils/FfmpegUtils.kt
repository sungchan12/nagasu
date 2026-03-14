//package com.mymedia.nagasu.utils
//
//import java.io.File
//import java.util.concurrent.TimeUnit
//import org.slf4j.LoggerFactory
//
//private val ffmpegLogger = LoggerFactory.getLogger("com.mymedia.nagasu.utils.FfmpegUtils")
//
///**
// * 비디오 파일에서 썸네일을 추출한다.
// * @param videoFile 비디오 파일
// * @param outputFile 썸네일 출력 파일 (jpg)
// * @param timeSeconds 추출할 시간 (초), 기본값 1초
// * @return 성공 여부
// */
//fun extractThumbnail(videoFile: File, outputFile: File, timeSeconds: Int = 1): Boolean {
//    if (!videoFile.exists()) {
//        ffmpegLogger.warn("Thumbnail extraction failed: video file not found - ${videoFile.absolutePath}")
//        return false
//    }
//
//    val command = listOf(
//        "ffmpeg",
//        "-y",                          // 덮어쓰기
//        "-ss", timeSeconds.toString(), // 시간 위치
//        "-i", videoFile.absolutePath,  // 입력 파일
//        "-vframes", "1",               // 1프레임만
//        "-q:v", "2",                   // 품질 (2가 높은 품질)
//        outputFile.absolutePath        // 출력 파일
//    )
//
//    return try {
//        val process = ProcessBuilder(command)
//            .redirectErrorStream(true)
//            .start()
//        val output = process.inputStream.bufferedReader().readText()
//        val completed = process.waitFor(30, TimeUnit.SECONDS)
//        if (!completed) {
//            process.destroyForcibly()
//            ffmpegLogger.warn("FFmpeg thumbnail extraction timed out")
//            if (outputFile.exists()) outputFile.delete()
//            return false
//        }
//        val exitCode = process.exitValue()
//        if (exitCode != 0) {
//            ffmpegLogger.warn("FFmpeg thumbnail extraction failed (exitCode=$exitCode):\n$output")
//            if (outputFile.exists()) outputFile.delete()
//            return false
//        }
//        true
//    } catch (e: Exception) {
//        ffmpegLogger.error("FFmpeg execution error: ${e.message}", e)
//        if (outputFile.exists()) outputFile.delete()
//        false
//    }
//}
//
///**
// * 비디오 파일에서 썸네일을 추출하여 같은 폴더에 thumbnail.jpg로 저장한다.
// * @param videoFile 비디오 파일
// * @param timeSeconds 추출할 시간 (초), 기본값 1초
// * @return 생성된 썸네일 파일, 실패 시 null
// */
//fun extractThumbnailToFolder(videoFile: File, timeSeconds: Int = 1): File? {
//    val thumbnailFile = File(videoFile.parentFile, "thumbnail.jpg")
//    return if (extractThumbnail(videoFile, thumbnailFile, timeSeconds)) {
//        thumbnailFile
//    } else {
//        null
//    }
//}
//
///**
// * 자막을 vtt 형식으로 변환시킵니다.
// * @param subtitle 원본 자막 파일 (srt, ass 등)
// * @param outputFile 출력 vtt 파일
// * @return 변환된 vtt 파일
// */
//fun convertToVtt(subtitle: File, outputFile: File, timeoutSeconds: Long = 60): File {
//    val command = listOf(
//        "ffmpeg",
//        "-y",                       // 덮어쓰기
//        "-i", subtitle.absolutePath, // 입력 파일
//        outputFile.absolutePath      // 출력 파일
//    )
//    return try {
//        val process = ProcessBuilder(command).redirectErrorStream(true).start()
//        process.inputStream.bufferedReader().use { reader ->
//            reader.forEachLine { line ->
//                ffmpegLogger.debug("[FFmpeg] $line")
//            }
//        }
//        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
//        if (!completed) {
//            process.destroyForcibly()
//            throw RuntimeException("FFmpeg conversion timed out: exceeded ${timeoutSeconds}s")
//        }
//        if (process.exitValue() != 0 || !outputFile.exists()) {
//            throw RuntimeException("FFmpeg conversion failed. exitCode: ${process.exitValue()}")
//        }
//        outputFile
//    } catch (e: Exception) {
//        if (outputFile.exists()) outputFile.delete()
//        throw e
//    }
//}