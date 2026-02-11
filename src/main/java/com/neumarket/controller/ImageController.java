package com.neumarket.controller;

import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.ListingResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ImageController {

  private final ImageService imageService;

  /**
   * Upload images for a listing
   * POST /api/listings/{listingId}/images
   */
  @PostMapping(value = "/api/listings/{listingId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ListingResponse.ImageResponse>> uploadImages(
      @PathVariable Long listingId,
      @RequestParam("files") List<MultipartFile> files,
      @CurrentUser UserPrincipal currentUser) {

    List<ListingResponse.ImageResponse> images = imageService.uploadImages(listingId, files, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(images);
  }

  /**
   * Delete an image
   * DELETE /api/images/{imageId}
   */
  @DeleteMapping("/api/images/{imageId}")
  public ResponseEntity<ApiResponse> deleteImage(
      @PathVariable Long imageId,
      @CurrentUser UserPrincipal currentUser) {

    imageService.deleteImage(imageId, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
  }
}