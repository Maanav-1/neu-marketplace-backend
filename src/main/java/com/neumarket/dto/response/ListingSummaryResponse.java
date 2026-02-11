package com.neumarket.dto.response;

import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.enums.ListingStatus;
import com.neumarket.model.Listing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingSummaryResponse {

  private Long id;
  private String slug;
  private String title;
  private BigDecimal price;
  private Category category;
  private String categoryDisplayName;
  private Condition condition;
  private String conditionDisplayName;
  private ListingStatus status;
  private String thumbnailUrl; // First image
  private String sellerName;
  private LocalDateTime createdAt;
  private Boolean isSaved;
  private LocalDateTime expiresAt;

  // Factory method to create from entity
  public static ListingSummaryResponse fromEntity(Listing listing, Boolean isSaved) {
    String thumbnail = listing.getImages().isEmpty()
        ? null
        : listing.getImages().get(0).getImageUrl();

    return ListingSummaryResponse.builder()
        .id(listing.getId())
        .slug(listing.getSlug())
        .title(listing.getTitle())
        .price(listing.getPrice())
        .category(listing.getCategory())
        .categoryDisplayName(listing.getCategory().getDisplayName())
        .condition(listing.getCondition())
        .conditionDisplayName(listing.getCondition().getDisplayName())
        .status(listing.getStatus())
        .thumbnailUrl(thumbnail)
        .sellerName(listing.getUser().getName())
        .createdAt(listing.getCreatedAt())
        .isSaved(isSaved)
        .expiresAt(listing.getExpiresAt())
        .build();
  }
}