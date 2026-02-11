package com.neumarket.dto.response;

import com.neumarket.enums.Role;
import com.neumarket.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {

  private Long id;
  private String email;
  private String name;
  private String phone;
  private String profilePicUrl;
  private Role role;
  private Boolean emailVerified;
  private Boolean blocked;
  private String blockReason;
  private LocalDateTime blockedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Stats
  private int listingsCount;
  private int conversationsCount;

  public static AdminUserResponse fromEntity(User user, int listingsCount, int conversationsCount) {
    return AdminUserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .phone(user.getPhone())
        .profilePicUrl(user.getProfilePicUrl())
        .role(user.getRole())
        .emailVerified(user.getEmailVerified())
        .blocked(user.getBlocked())
        .blockReason(user.getBlockReason())
        .blockedAt(user.getBlockedAt())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .listingsCount(listingsCount)
        .conversationsCount(conversationsCount)
        .build();
  }

  public static AdminUserResponse fromEntity(User user) {
    return fromEntity(user, 0, 0);
  }
}