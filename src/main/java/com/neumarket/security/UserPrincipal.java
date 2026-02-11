package com.neumarket.security;

import com.neumarket.enums.Role;
import com.neumarket.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UserPrincipal implements UserDetails, OAuth2User {

  private Long id;
  private String email;
  private String password;
  private String name;
  private Boolean emailVerified;
  private Boolean blocked;
  private Role role;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

  public static UserPrincipal create(User user) {
    return create(user, null);
  }

  public static UserPrincipal create(User user, Map<String, Object> attributes) {
    String roleAuthority = "ROLE_" + user.getRole().name();
    return UserPrincipal.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(user.getPasswordHash())
        .name(user.getName())
        .emailVerified(user.getEmailVerified())
        .blocked(user.getBlocked())
        .role(user.getRole())
        .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleAuthority)))
        .attributes(attributes)
        .build();
  }

  @Override
  public String getUsername() { return email; }

  @Override
  public String getPassword() { return password; }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

  @Override
  public Map<String, Object> getAttributes() { return attributes; }

  @Override
  public String getName() { return String.valueOf(id); }

  @Override
  public boolean isAccountNonExpired() { return true; }

  @Override
  public boolean isAccountNonLocked() { return !blocked; }

  @Override
  public boolean isCredentialsNonExpired() { return true; }

  @Override
  public boolean isEnabled() { return true; }
}