package com.neumarket.dto.response;

import com.neumarket.model.Conversation;
import com.neumarket.model.Message;
import com.neumarket.model.User;
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
public class ConversationResponse {

  private Long id;
  private ListingInfo listing;
  private ParticipantInfo otherParticipant;
  private String lastMessage;
  private LocalDateTime lastMessageAt;
  private int unreadCount;
  private Boolean isBuyer;    // true = current user is buyer, false = current user is seller
  private Boolean isSeller;   // true = current user is seller, false = current user is buyer
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ListingInfo {
    private Long id;
    private String slug;
    private String title;
    private BigDecimal price;
    private String thumbnailUrl;
    private String status;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ParticipantInfo {
    private Long id;
    private String name;
    private String profilePicUrl;
  }

  public static ConversationResponse fromEntity(Conversation conversation, Long currentUserId, int unreadCount) {
    // Determine if current user is buyer or seller
    boolean isBuyer = conversation.getBuyer().getId().equals(currentUserId);
    boolean isSeller = conversation.getSeller().getId().equals(currentUserId);

    // The "other" participant is the one who is NOT the current user
    User otherUser = isBuyer ? conversation.getSeller() : conversation.getBuyer();

    // Get last message info
    String lastMsg = null;
    LocalDateTime lastMsgAt = null;
    if (!conversation.getMessages().isEmpty()) {
      Message last = conversation.getMessages().get(conversation.getMessages().size() - 1);
      lastMsg = last.getContent();
      lastMsgAt = last.getCreatedAt();
    }

    // Get thumbnail (first image)
    String thumbnail = conversation.getListing().getImages().isEmpty()
        ? null
        : conversation.getListing().getImages().get(0).getImageUrl();

    return ConversationResponse.builder()
        .id(conversation.getId())
        .listing(ListingInfo.builder()
            .id(conversation.getListing().getId())
            .slug(conversation.getListing().getSlug())
            .title(conversation.getListing().getTitle())
            .price(conversation.getListing().getPrice())
            .thumbnailUrl(thumbnail)
            .status(conversation.getListing().getStatus().name())
            .build())
        .otherParticipant(ParticipantInfo.builder()
            .id(otherUser.getId())
            .name(otherUser.getName())
            .profilePicUrl(otherUser.getProfilePicUrl())
            .build())
        .lastMessage(lastMsg)
        .lastMessageAt(lastMsgAt)
        .unreadCount(unreadCount)
        .isBuyer(isBuyer)
        .isSeller(isSeller)
        .createdAt(conversation.getCreatedAt())
        .updatedAt(conversation.getUpdatedAt())
        .build();
  }
}