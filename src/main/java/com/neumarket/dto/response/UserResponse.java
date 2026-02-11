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
public class UserResponse {

  private Long id;
  private String email;
  private String name;
  private String phone;
  private String profilePicUrl;
  private Boolean emailVerified;
  private Role role;
  private LocalDateTime memberSince;

  // Full response (for own profile)
  public static UserResponse fromEntity(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .phone(user.getPhone())
        .profilePicUrl(user.getProfilePicUrl())
        .emailVerified(user.getEmailVerified())
        .role(user.getRole())
        .memberSince(user.getCreatedAt())
        .build();
  }

  // Public response (for other users - hide email & phone)
  public static UserResponse publicFromEntity(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .profilePicUrl(user.getProfilePicUrl())
        .memberSince(user.getCreatedAt())
        .build();
  }
}