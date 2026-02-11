package com.neumarket.controller;

import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.SavedItemResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.SavedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved")
@RequiredArgsConstructor
public class SavedItemController {

  private final SavedItemService savedItemService;

  /**
   * Get all saved items for current user
   * GET /api/saved
   */
  @GetMapping
  public ResponseEntity<List<SavedItemResponse>> getSavedItems(@CurrentUser UserPrincipal currentUser) {
    List<SavedItemResponse> savedItems = savedItemService.getSavedItems(currentUser.getId());
    return ResponseEntity.ok(savedItems);
  }

  /**
   * Save a listing
   * POST /api/saved/{listingId}
   */
  @PostMapping("/{listingId}")
  public ResponseEntity<SavedItemResponse> saveListing(
      @PathVariable Long listingId,
      @CurrentUser UserPrincipal currentUser) {

    SavedItemResponse response = savedItemService.saveListing(listingId, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Unsave a listing
   * DELETE /api/saved/{listingId}
   */
  @DeleteMapping("/{listingId}")
  public ResponseEntity<ApiResponse> unsaveListing(
      @PathVariable Long listingId,
      @CurrentUser UserPrincipal currentUser) {

    savedItemService.unsaveListing(listingId, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Listing removed from saved items"));
  }

  /**
   * Check if listing is saved
   * GET /api/saved/{listingId}/check
   */
  @GetMapping("/{listingId}/check")
  public ResponseEntity<Map<String, Boolean>> checkIfSaved(
      @PathVariable Long listingId,
      @CurrentUser UserPrincipal currentUser) {

    boolean isSaved = savedItemService.isListingSaved(listingId, currentUser.getId());
    return ResponseEntity.ok(Map.of("isSaved", isSaved));
  }
}