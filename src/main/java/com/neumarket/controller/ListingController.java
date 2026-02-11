package com.neumarket.controller;

import com.neumarket.dto.request.ListingRequest;
import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.ListingResponse;
import com.neumarket.dto.response.ListingSummaryResponse;
import com.neumarket.dto.response.PagedResponse;
import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

  private final ListingService listingService;

  /**
   * Browse listings with filters (public)
   * GET /api/listings?category=ELECTRONICS&minPrice=50&maxPrice=500&search=iphone&sort=newest&page=0&size=20
   */
  @GetMapping
  public ResponseEntity<PagedResponse<ListingSummaryResponse>> getListings(
      @RequestParam(required = false) Category category,
      @RequestParam(required = false) Condition condition,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "newest") String sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @CurrentUser UserPrincipal currentUser) {

    Long currentUserId = currentUser != null ? currentUser.getId() : null;

    PagedResponse<ListingSummaryResponse> response = listingService.getListings(
        category, condition, minPrice, maxPrice, search, sort, page, size, currentUserId
    );

    return ResponseEntity.ok(response);
  }

  /**
   * Get single listing by slug (public)
   * GET /api/listings/{slug}
   */
  @GetMapping("/{slug}")
  public ResponseEntity<ListingResponse> getListingBySlug(
      @PathVariable String slug,
      @CurrentUser UserPrincipal currentUser) {

    Long currentUserId = currentUser != null ? currentUser.getId() : null;
    ListingResponse response = listingService.getListingBySlug(slug, currentUserId);

    return ResponseEntity.ok(response);
  }

  /**
   * Create new listing (authenticated)
   * POST /api/listings
   */
  @PostMapping
  public ResponseEntity<ListingResponse> createListing(
      @Valid @RequestBody ListingRequest request,
      @CurrentUser UserPrincipal currentUser) {

    ListingResponse response = listingService.createListing(request, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update listing (owner only)
   * PUT /api/listings/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<ListingResponse> updateListing(
      @PathVariable Long id,
      @Valid @RequestBody ListingRequest request,
      @CurrentUser UserPrincipal currentUser) {

    ListingResponse response = listingService.updateListing(id, request, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Delete listing (owner only)
   * DELETE /api/listings/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteListing(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    listingService.deleteListing(id, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully"));
  }

  /**
   * Mark listing as sold (owner only)
   * PATCH /api/listings/{id}/sold
   */
  @PatchMapping("/{id}/sold")
  public ResponseEntity<ListingResponse> markAsSold(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    ListingResponse response = listingService.markAsSold(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Bump/renew listing (owner only)
   * PATCH /api/listings/{id}/bump
   */
  @PatchMapping("/{id}/bump")
  public ResponseEntity<ListingResponse> bumpListing(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    ListingResponse response = listingService.bumpListing(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }
}