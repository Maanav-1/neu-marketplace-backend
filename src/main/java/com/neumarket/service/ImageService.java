package com.neumarket.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.neumarket.dto.response.ListingResponse;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ForbiddenException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Listing;
import com.neumarket.model.ListingImage;
import com.neumarket.repository.ListingImageRepository;
import com.neumarket.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

  private final ListingRepository listingRepository;
  private final ListingImageRepository listingImageRepository;

  @Autowired(required = false)
  private BlobContainerClient blobContainerClient;

  @Value("${azure.storage.container-name:listings-images}")
  private String containerName;

  @Value("${app.images.max-file-size-mb:5}")
  private int maxFileSizeMb;

  private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
      "image/jpeg", "image/jpg", "image/png", "image/webp"
  );

  @Transactional
  public List<ListingResponse.ImageResponse> uploadImages(Long listingId, List<MultipartFile> files, Long userId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

    if (!listing.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only upload images to your own listings");
    }

    List<ListingResponse.ImageResponse> uploadedImages = new ArrayList<>();
    int displayOrder = listingImageRepository.findMaxDisplayOrderByListingId(listingId);

    for (MultipartFile file : files) {
      validateFile(file);
      String imageUrl = uploadToStorage(file, listingId);
      displayOrder++;

      ListingImage image = ListingImage.builder()
          .listing(listing)
          .imageUrl(imageUrl)
          .displayOrder(displayOrder)
          .build();

      ListingImage saved = listingImageRepository.save(image);
      uploadedImages.add(ListingResponse.ImageResponse.builder()
          .id(saved.getId())
          .imageUrl(saved.getImageUrl())
          .displayOrder(saved.getDisplayOrder())
          .build());
    }

    return uploadedImages;
  }

  @Transactional
  public void deleteImage(Long imageId, Long userId) {
    ListingImage image = listingImageRepository.findById(imageId)
        .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

    if (!image.getListing().getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only delete images from your own listings");
    }

    deleteFromStorage(image.getImageUrl());
    listingImageRepository.delete(image);
    log.info("Image deleted: {}", imageId);
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BadRequestException("File is empty");
    }

    long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
    if (file.getSize() > maxBytes) {
      throw new BadRequestException("File size exceeds maximum limit of " + maxFileSizeMb + "MB");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      throw new BadRequestException("Invalid file type. Allowed: JPEG, PNG, WebP");
    }
  }

  private String uploadToStorage(MultipartFile file, Long listingId) {
    String filename = generateFilename(file, listingId);

    if (blobContainerClient != null) {
      try {
        BlobClient blobClient = blobContainerClient.getBlobClient(filename);
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        return blobClient.getBlobUrl();
      } catch (IOException e) {
        log.error("Failed to upload image to Azure", e);
        throw new BadRequestException("Failed to upload image");
      }
    }

    log.warn("Azure Blob Storage not configured. Using placeholder URL.");
    return "https://placeholder.com/images/" + filename;
  }

  private void deleteFromStorage(String imageUrl) {
    if (blobContainerClient != null && imageUrl.contains(containerName)) {
      try {
        String blobName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.deleteIfExists();
      } catch (Exception e) {
        log.error("Failed to delete image from Azure: {}", imageUrl, e);
      }
    }
  }

  private String generateFilename(MultipartFile file, Long listingId) {
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    return "listing-" + listingId + "/" + UUID.randomUUID() + extension;
  }
}