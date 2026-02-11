package com.neumarket.controller;

import com.neumarket.dto.request.UpdateUserRequest;
import com.neumarket.dto.response.ListingSummaryResponse;
import com.neumarket.dto.response.UserResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.ListingService;
import com.neumarket.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ListingService listingService;

  /**
   * Get current user's profile (authenticated)
   * GET /api/users/me
   */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
    UserResponse response = userService.getCurrentUser(currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Update current user's profile (authenticated)
   * PUT /api/users/me
   */
  @PutMapping("/me")
  public ResponseEntity<UserResponse> updateProfile(
      @Valid @RequestBody UpdateUserRequest request,
      @CurrentUser UserPrincipal currentUser) {

    UserResponse response = userService.updateProfile(currentUser.getId(), request);
    return ResponseEntity.ok(response);
  }

  /**
   * Get public profile of a user (public)
   * GET /api/users/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getPublicProfile(@PathVariable Long id) {
    UserResponse response = userService.getPublicProfile(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get listings by user (public)
   * GET /api/users/{id}/listings
   */
  @GetMapping("/{id}/listings")
  public ResponseEntity<List<ListingSummaryResponse>> getUserListings(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    Long currentUserId = currentUser != null ? currentUser.getId() : null;
    List<ListingSummaryResponse> listings = listingService.getListingsByUser(id, currentUserId);

    return ResponseEntity.ok(listings);
  }
}