package com.mymedia.nagasu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FfmpegUtils {

    private static final Logger log = LoggerFactory.getLogger(FfmpegUtils.class);

    private FfmpegUtils() {}

    /**
     * 비디오 파일에서 썸네일을 추출한다.
     * @param videoFile 비디오 파일
     * @param outputFile 썸네일 출력 파일 (jpg)
     * @param timeSeconds 추출할 시간 (초)
     * @return 성공 여부
     */
    public static boolean extractThumbnail(File videoFile, File outputFile, int timeSeconds) {
        if (!videoFile.exists()) {
            log.warn("Thumbnail extraction failed: video file not found - {}", videoFile.getAbsolutePath());
            return false;
        }

        var command = List.of(
                "ffmpeg",
                "-y",                                  // 덮어쓰기
                "-ss", String.valueOf(timeSeconds),    // 시간 위치
                "-i", videoFile.getAbsolutePath(),     // 입력 파일
                "-vframes", "1",                       // 1프레임만
                "-q:v", "2",                           // 품질 (2가 높은 품질)
                outputFile.getAbsolutePath()           // 출력 파일
        );

        try {
            var process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            var output = new String(process.getInputStream().readAllBytes());
            var completed = process.waitFor(30, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.warn("FFmpeg thumbnail extraction timed out");
                Files.deleteIfExists(outputFile.toPath());
                return false;
            }
            var exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("FFmpeg thumbnail extraction failed (exitCode={}):\n{}", exitCode, output);
                Files.deleteIfExists(outputFile.toPath());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("FFmpeg execution error: {}", e.getMessage(), e);
            try { Files.deleteIfExists(outputFile.toPath()); } catch (IOException ex) { log.warn("Failed to clean up file: {}", outputFile.getAbsolutePath()); }
            return false;
        }
    }

    public static boolean extractThumbnail(File videoFile, File outputFile) {
        return extractThumbnail(videoFile, outputFile, 1);
    }

    /**
     * 비디오 파일에서 썸네일을 추출하여 같은 폴더에 thumbnail.jpg로 저장한다.
     * @param videoFile 비디오 파일
     * @param timeSeconds 추출할 시간 (초)
     * @return 생성된 썸네일 파일, 실패 시 null
     */
    public static File extractThumbnailToFolder(File videoFile, int timeSeconds) {
        var thumbnailFile = new File(videoFile.getParentFile(), "thumbnail.jpg");
        return extractThumbnail(videoFile, thumbnailFile, timeSeconds) ? thumbnailFile : null;
    }

    public static File extractThumbnailToFolder(File videoFile) {
        return extractThumbnailToFolder(videoFile, 1);
    }

    /**
     * 자막을 vtt 형식으로 변환시킵니다.
     * @param subtitle 원본 자막 파일 (srt, ass 등)
     * @param outputFile 출력 vtt 파일
     * @param timeoutSeconds 타임아웃 (초)
     * @return 변환된 vtt 파일
     */
    public static File convertToVtt(File subtitle, File outputFile, long timeoutSeconds) {
        var command = List.of(
                "ffmpeg",
                "-y",                              // 덮어쓰기
                "-i", subtitle.getAbsolutePath(),  // 입력 파일
                outputFile.getAbsolutePath()       // 출력 파일
        );
        try {
            var process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.debug("[FFmpeg] {}", line));
            }
            var completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg conversion timed out: exceeded " + timeoutSeconds + "s");
            }
            if (process.exitValue() != 0 || !outputFile.exists()) {
                throw new RuntimeException("FFmpeg conversion failed. exitCode: " + process.exitValue());
            }
            return outputFile;
        } catch (RuntimeException e) {
            try { Files.deleteIfExists(outputFile.toPath()); } catch (IOException ex) { log.warn("Failed to clean up file: {}", outputFile.getAbsolutePath()); }
            throw e;
        } catch (Exception e) {
            try { Files.deleteIfExists(outputFile.toPath()); } catch (IOException ex) { log.warn("Failed to clean up file: {}", outputFile.getAbsolutePath()); }
            throw new RuntimeException(e);
        }
    }

    public static File convertToVtt(File subtitle, File outputFile) {
        return convertToVtt(subtitle, outputFile, 60);
    }
}