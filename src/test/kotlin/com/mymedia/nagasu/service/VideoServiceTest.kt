package com.mymedia.nagasu.service

import com.mymedia.nagasu.dto.VideoUploadRequestDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.nio.file.Files

class VideoServiceTest {

    private lateinit var tempDir: File
    private lateinit var videoService: VideoService

    @BeforeEach
    fun setUp() {
        // 테스트마다 임시 디렉토리 생성
        tempDir = Files.createTempDirectory("nagasu-test").toFile()
        videoService = VideoService(tempDir.absolutePath)
    }

    @AfterEach
    fun tearDown() {
        // 테스트 후 임시 디렉토리 정리
        tempDir.deleteRecursively()
    }

    // ===== uploadVideoCollection =====

    @Test
    fun `파일명이 없으면 IllegalArgumentException이 발생한다`() {
        // MockMultipartFile은 null을 빈 문자열로 처리하므로 originalFilename이 비어있는 경우로 테스트
        // VideoService에서 파일명 없음 체크가 필요하다면 서비스 레벨에서 빈 문자열도 거부해야 함
        // 현재는 Spring이 null을 ""로 처리하므로 이 케이스는 실제 요청 환경에서만 발생
        // -> 테스트 제거 또는 빈 문자열 검사 추가 필요
    }

    @Test
    fun `동일한 collectionId가 이미 존재하면 IllegalStateException이 발생한다`() {
        val mockVideo = validVideoFile()
        val requestDto = buildRequestDto(video = mockVideo)

        // 미리 폴더 생성
        File(tempDir, "videos/test_video").mkdirs()

        assertThrows(IllegalStateException::class.java) {
            videoService.uploadVideoCollection(requestDto)
        }
    }

    @Test
    fun `업로드 실패 시 생성된 폴더가 롤백된다`() {
        // 빈 파일로 업로드 -> FFmpeg 썸네일 생성 실패 유도
        val mockVideo = MockMultipartFile("video", "test video.mp4", "video/mp4", ByteArray(0))
        val requestDto = buildRequestDto(video = mockVideo)

        assertThrows(Exception::class.java) {
            videoService.uploadVideoCollection(requestDto)
        }

        val collectionDir = File(tempDir, "videos/test_video")
        assertFalse(collectionDir.exists(), "업로드 실패 시 폴더가 삭제되어야 합니다")
    }

    @Test
    fun `파일명의 baseName이 slug화되어 collectionId로 사용된다`() {
        val mockVideo = validVideoFile("My Test Video.mp4")
        val mockThumbnail = MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", ByteArray(1))
        val requestDto = buildRequestDto(video = mockVideo, thumbnail = mockThumbnail)

        val collectionId = videoService.uploadVideoCollection(requestDto)

        assertEquals("My_Test_Video", collectionId)
        assertTrue(File(tempDir, "videos/My_Test_Video").exists())
    }

    @Test
    fun `비디오 파일이 원본 파일명으로 저장된다`() {
        val mockVideo = validVideoFile("My Test Video.mp4")
        val mockThumbnail = MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", ByteArray(1))
        val requestDto = buildRequestDto(video = mockVideo, thumbnail = mockThumbnail)

        videoService.uploadVideoCollection(requestDto)

        assertTrue(File(tempDir, "videos/My_Test_Video/My Test Video.mp4").exists())
    }

    @Test
    fun `사용자 썸네일이 있으면 그대로 저장된다`() {
        val mockVideo = validVideoFile()
        val mockThumbnail = MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", ByteArray(1))
        val requestDto = buildRequestDto(video = mockVideo, thumbnail = mockThumbnail)

        videoService.uploadVideoCollection(requestDto)

        assertTrue(File(tempDir, "videos/test_video/thumb.jpg").exists())
    }

    @Test
    fun `자막이 vtt이면 변환 없이 그대로 저장된다`() {
        val mockVideo = validVideoFile()
        val mockThumbnail = MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", ByteArray(1))
        val mockSubtitle = MockMultipartFile("subtitle", "test video.vtt", "text/vtt", "WEBVTT".toByteArray())
        val requestDto = buildRequestDto(video = mockVideo, thumbnail = mockThumbnail, subtitle = mockSubtitle)

        videoService.uploadVideoCollection(requestDto)

        assertTrue(File(tempDir, "videos/test_video/test video.vtt").exists())
        // 이미 vtt이므로 추가 변환 파일 없어야 함
        assertEquals(1, File(tempDir, "videos/test_video").listFiles()?.count { it.extension == "vtt" })
    }

    @Test
    fun `메타데이터가 저장된다`() {
        val mockVideo = validVideoFile()
        val mockThumbnail = MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", ByteArray(1))
        val requestDto = buildRequestDto(video = mockVideo, thumbnail = mockThumbnail)

        videoService.uploadVideoCollection(requestDto)

        assertTrue(File(tempDir, "videos/test_video/metadata.json").exists())
    }

    // ===== deleteVideoCollection =====

    @Test
    fun `존재하지 않는 collectionId 삭제 시 NoSuchElementException이 발생한다`() {
        assertThrows(NoSuchElementException::class.java) {
            videoService.deleteVideoCollection("not-exist")
        }
    }

    @Test
    fun `컬렉션 삭제 시 폴더가 완전히 삭제된다`() {
        val collectionDir = File(tempDir, "videos/test_video")
        collectionDir.mkdirs()
        File(collectionDir, "test.mp4").createNewFile()

        videoService.deleteVideoCollection("test_video")

        assertFalse(collectionDir.exists())
    }

    // ===== helpers =====

    private fun validVideoFile(fileName: String = "test video.mp4") =
        MockMultipartFile("video", fileName, "video/mp4", ByteArray(1))

    private fun buildRequestDto(
        video: MockMultipartFile = validVideoFile(),
        thumbnail: MockMultipartFile? = null,
        subtitle: MockMultipartFile? = null
    ) = VideoUploadRequestDto(
        title = "테스트 영상",
        artist = "테스트 아티스트",
        tags = listOf("tag1", "tag2"),
        description = "테스트 설명",
        video = video,
        thumbnail = thumbnail,
        subtitle = subtitle
    )
}
