package com.neumarket.controller;

import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

  /**
   * Public endpoint - no auth needed
   * GET /api/test/public
   */
  @GetMapping("/public")
  public ResponseEntity<Map<String, String>> publicEndpoint() {
    return ResponseEntity.ok(Map.of(
        "message", "This is a public endpoint - no authentication required",
        "status", "success"
    ));
  }

  /**
   * Protected endpoint - requires JWT token
   * GET /api/test/protected
   */
  @GetMapping("/protected")
  public ResponseEntity<Map<String, Object>> protectedEndpoint(@CurrentUser UserPrincipal user) {
    return ResponseEntity.ok(Map.of(
        "message", "This is a protected endpoint - you are authenticated!",
        "userId", user.getId(),
        "email", user.getEmail(),
        "name", user.getName()
    ));
  }
}