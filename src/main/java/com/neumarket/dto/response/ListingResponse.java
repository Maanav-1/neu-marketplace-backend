package com.neumarket.dto.response;

import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.enums.ListingStatus;
import com.neumarket.model.Listing;
import com.neumarket.model.ListingImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingResponse {

  private Long id;
  private String slug;
  private String title;
  private String description;
  private BigDecimal price;
  private Category category;
  private String categoryDisplayName;
  private Condition condition;
  private String conditionDisplayName;
  private ListingStatus status;
  private List<ImageResponse> images;
  private SellerResponse seller;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
  private Boolean isSaved; // Whether current user saved this listing

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ImageResponse {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SellerResponse {
    private Long id;
    private String name;
    private String profilePicUrl;
    private LocalDateTime memberSince;
  }

  // Factory method to create from entity
  public static ListingResponse fromEntity(Listing listing, Boolean isSaved) {
    return ListingResponse.builder()
        .id(listing.getId())
        .slug(listing.getSlug())
        .title(listing.getTitle())
        .description(listing.getDescription())
        .price(listing.getPrice())
        .category(listing.getCategory())
        .categoryDisplayName(listing.getCategory().getDisplayName())
        .condition(listing.getCondition())
        .conditionDisplayName(listing.getCondition().getDisplayName())
        .status(listing.getStatus())
        .images(listing.getImages().stream()
            .map(img -> ImageResponse.builder()
                .id(img.getId())
                .imageUrl(img.getImageUrl())
                .displayOrder(img.getDisplayOrder())
                .build())
            .toList())
        .seller(SellerResponse.builder()
            .id(listing.getUser().getId())
            .name(listing.getUser().getName())
            .profilePicUrl(listing.getUser().getProfilePicUrl())
            .memberSince(listing.getUser().getCreatedAt())
            .build())
        .createdAt(listing.getCreatedAt())
        .expiresAt(listing.getExpiresAt())
        .isSaved(isSaved)
        .build();
  }
}