package com.mymedia.nagasu.controller;

import com.mymedia.nagasu.dto.ApiResponse;
import com.mymedia.nagasu.dto.ImageCollectionResponse;
import com.mymedia.nagasu.dto.ImageDetailsResponse;
import com.mymedia.nagasu.dto.ImageUpdateDto;
import com.mymedia.nagasu.dto.ImageUploadDto;
import com.mymedia.nagasu.service.ImageService;
import com.mymedia.nagasu.service.PrivateSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Image collection API controller
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final PrivateSessionService privateSessionService;

    /**
     * Returns all image collections.
     */
    @GetMapping
    public List<ImageCollectionResponse> getCollections(
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        var includePrivate = privateSessionService.isValidSession(sessionToken);
        return imageService.getCollections(sort, order, includePrivate);
    }

    /**
     * Returns collection details.
     */
    @GetMapping("/{collectionId}/details")
    public ResponseEntity<ImageDetailsResponse> getCollectionDetails(
            @PathVariable String collectionId,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        var includePrivate = privateSessionService.isValidSession(sessionToken);
        var details = imageService.getCollectionDetails(collectionId, includePrivate);
        return ResponseEntity.ok(details);
    }

    /**
     * Creates a new image collection.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> createCollection(
            @ModelAttribute ImageUploadDto requestDto,
            @CookieValue(value = "_sid", required = false) String sessionToken) {
        var isPrivate = privateSessionService.isValidSession(sessionToken);
        var result = imageService.createCollection(requestDto, isPrivate);
        return ResponseEntity.ok(new ApiResponse.Success<>(result));
    }

    /**
     * Adds images to an existing collection.
     */
    @PostMapping(value = "/{collectionId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> addCollectionImages(
            @PathVariable String collectionId,
            @RequestParam("images") List<MultipartFile> images) {
        imageService.addCollectionImages(collectionId, images);
        return ResponseEntity.ok(new ApiResponse.Success<>("Collection added " + images.size() + " images"));
    }

    /**
     * Deletes a collection.
     */
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<String>> deleteCollection(
            @PathVariable String collectionId) {
        imageService.deleteCollection(collectionId);
        return ResponseEntity.ok(new ApiResponse.Success<>("Collection deleted Successfully!"));
    }

    /**
     * Updates collection metadata and thumbnail.
     */
    @PatchMapping(value = "/{collectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> updateCollection(
            @ModelAttribute ImageUpdateDto imageUpdateRequest,
            @PathVariable String collectionId) {
        imageService.updateCollectionMetaData(collectionId, imageUpdateRequest);
        return ResponseEntity.ok(new ApiResponse.Success<>("Collection Updated Successfully!"));
    }

    @PostMapping("/{id}/repair")
    public ResponseEntity<ApiResponse<String>> repairVideoCollection(
            @PathVariable String id) {
        imageService.repairImageCollection(id);
        return ResponseEntity.ok(new ApiResponse.Success<>("Video repaired successfully"));
    }
}