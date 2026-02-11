package com.neumarket.dto.response;

import com.neumarket.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

  private String token;
  private String tokenType;
  private Long expiresIn;
  private UserSummaryResponse user;
  private Role role;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UserSummaryResponse {
    private Long id;
    private String email;
    private String name;
    private String profilePicUrl;
    private Boolean emailVerified;
    private Role role;
  }
}