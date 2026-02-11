package com.neumarket.security;

import com.neumarket.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  private SecretKey key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Main token generation logic using UserPrincipal
   */
  public String generateToken(UserPrincipal userPrincipal) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    return Jwts.builder()
        .subject(Long.toString(userPrincipal.getId()))
        .claim("email", userPrincipal.getEmail())
        .claim("name", userPrincipal.getName())
        .claim("role", userPrincipal.getRole().name())
        .claim("verified", userPrincipal.getEmailVerified())
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String generateToken(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    return generateToken(userPrincipal);
  }

  /**
   * Reconstructs UserPrincipal from token claims - NO DATABASE HIT
   */
  public UserPrincipal getUserPrincipalFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    Role role = Role.valueOf(claims.get("role", String.class));

    return UserPrincipal.builder()
        .id(Long.parseLong(claims.getSubject()))
        .email(claims.get("email", String.class))
        .name(claims.get("name", String.class))
        .role(role)
        .emailVerified(claims.get("verified", Boolean.class))
        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name())))
        .blocked(false)
        .build();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      log.error("JWT validation failed: {}", ex.getMessage());
      return false;
    }
  }

  public long getExpirationInSeconds() {
    return jwtExpiration / 1000;
  }
}