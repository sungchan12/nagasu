package com.mymedia.nagasu.controller;

import com.mymedia.nagasu.dto.ApiResponse;
import com.mymedia.nagasu.dto.VideoCollectionResponse;
import com.mymedia.nagasu.dto.VideoDetailsResponse;
import com.mymedia.nagasu.dto.VideoUploadRequestDto;
import com.mymedia.nagasu.service.PrivateSessionService;
import com.mymedia.nagasu.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Video collection API controller
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final PrivateSessionService privateSessionService;

    /**
     * Returns all video collections.
     */
    @GetMapping
    public List<VideoCollectionResponse> getVideos(
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(name = "private", defaultValue = "false") boolean privateMode,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        var includePrivate = privateMode && privateSessionService.isValidSession(sessionToken);
        return videoService.getVideoCollection(sort, order, includePrivate);
    }

    /**
     * Returns video collection details.
     */
    @GetMapping("{id}/details")
    public VideoDetailsResponse getVideoCollectionDetails(
            @PathVariable String id,
            @RequestParam(name = "private", defaultValue = "false") boolean privateMode,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        var isPrivate = privateMode && privateSessionService.isValidSession(sessionToken);
        return videoService.getVideoCollectionDetails(id, isPrivate);
    }

    /**
     * Uploads a new video collection.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> uploadVideoCollection(
            @ModelAttribute VideoUploadRequestDto requestDto,
            @RequestParam(name = "private", defaultValue = "false") boolean privateMode,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        if (!privateSessionService.isValidSession(sessionToken)) return ResponseEntity.status(401).build();
        var collectionId = videoService.uploadVideoCollection(requestDto, privateMode);
        return ResponseEntity.ok(new ApiResponse.Success<>(collectionId));
    }

    /**
     * Deletes a video collection.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVideoCollection(
            @PathVariable String id,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        if (!privateSessionService.isValidSession(sessionToken)) return ResponseEntity.status(401).build();
        videoService.deleteVideoCollection(id);
        return ResponseEntity.ok(new ApiResponse.Success<>("Video deleted successfully"));
    }

    /**
     * Repairs a manually added video collection (generates missing metadata, thumbnail, subtitles).
     */
    @PostMapping("{id}/repair")
    public ResponseEntity<ApiResponse<String>> repairVideoCollection(
            @PathVariable String id,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        if (!privateSessionService.isValidSession(sessionToken)) return ResponseEntity.status(401).build();
        videoService.repairVideoCollection(id);
        return ResponseEntity.ok(new ApiResponse.Success<>("Video repaired successfully"));
    }
}