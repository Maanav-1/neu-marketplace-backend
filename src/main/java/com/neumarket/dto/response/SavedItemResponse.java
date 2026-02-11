package com.neumarket.dto.response;

import com.neumarket.model.SavedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedItemResponse {

  private Long id;
  private ListingSummaryResponse listing;
  private LocalDateTime savedAt;

  public static SavedItemResponse fromEntity(SavedItem savedItem) {
    return SavedItemResponse.builder()
        .id(savedItem.getId())
        .listing(ListingSummaryResponse.fromEntity(savedItem.getListing(), true))
        .savedAt(savedItem.getCreatedAt())
        .build();
  }
}