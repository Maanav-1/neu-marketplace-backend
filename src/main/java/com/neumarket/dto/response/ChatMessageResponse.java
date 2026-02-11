package com.neumarket.dto.response;

import com.neumarket.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

  private Long id;
  private Long conversationId;
  private SenderInfo sender;
  private String content;
  private Boolean isRead;
  private Boolean isOwnMessage; // Helper for frontend
  private LocalDateTime createdAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SenderInfo {
    private Long id;
    private String name;
    private String profilePicUrl;
  }

  public static ChatMessageResponse fromEntity(Message message, Long currentUserId) {
    return ChatMessageResponse.builder()
        .id(message.getId())
        .conversationId(message.getConversation().getId())
        .sender(SenderInfo.builder()
            .id(message.getSender().getId())
            .name(message.getSender().getName())
            .profilePicUrl(message.getSender().getProfilePicUrl())
            .build())
        .content(message.getContent())
        .isRead(message.getIsRead())
        .isOwnMessage(message.getSender().getId().equals(currentUserId))
        .createdAt(message.getCreatedAt())
        .build();
  }
}