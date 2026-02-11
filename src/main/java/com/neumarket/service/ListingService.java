package com.neumarket.service;

import com.neumarket.dto.request.ListingRequest;
import com.neumarket.dto.response.ListingResponse;
import com.neumarket.dto.response.ListingSummaryResponse;
import com.neumarket.dto.response.PagedResponse;
import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.enums.ListingStatus;
import com.neumarket.exception.ForbiddenException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Listing;
import com.neumarket.model.User;
import com.neumarket.repository.ListingRepository;
import com.neumarket.repository.SavedItemRepository;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

  private final ListingRepository listingRepository;
  private final UserRepository userRepository;
  private final SavedItemRepository savedItemRepository;

  @Value("${app.listings.expiry-days:30}")
  private int expiryDays;

  /**
   * Create a new listing
   */
  @Transactional
  public ListingResponse createListing(ListingRequest request, Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    Listing listing = Listing.builder()
        .user(user)
        .title(request.getTitle().trim())
        .description(request.getDescription() != null ? request.getDescription().trim() : null)
        .price(request.getPrice())
        .category(request.getCategory())
        .condition(request.getCondition())
        .status(ListingStatus.ACTIVE)
        .slug(generateSlug(request.getTitle()))
        .expiresAt(LocalDateTime.now().plusDays(expiryDays))
        .build();

    Listing saved = listingRepository.save(listing);
    log.info("Listing created: {} by user {}", saved.getSlug(), userId);

    return ListingResponse.fromEntity(saved, false);
  }

  /**
   * Get listing by slug (public)
   */
  @Transactional(readOnly = true)
  public ListingResponse getListingBySlug(String slug, Long currentUserId) {
    Listing listing = listingRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "slug", slug));

    Boolean isSaved = currentUserId != null
        && savedItemRepository.existsByUserIdAndListingId(currentUserId, listing.getId());

    return ListingResponse.fromEntity(listing, isSaved);
  }

  /**
   * Get listing by ID (internal use)
   */
  @Transactional(readOnly = true)
  public Listing getListingById(Long id) {
    return listingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", id));
  }

  /**
   * Browse listings with filters
   */
  @Transactional(readOnly = true)
  public PagedResponse<ListingSummaryResponse> getListings(
      Category category,
      Condition condition,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String search,
      String sortBy,
      int page,
      int size,
      Long currentUserId) {

    Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
      case "price_asc" -> Sort.by("price").ascending();
      case "price_desc" -> Sort.by("price").descending();
      case "oldest" -> Sort.by("createdAt").ascending();
      default -> Sort.by("createdAt").descending();
    };

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<Listing> listingsPage = listingRepository.findWithFilters(
        ListingStatus.ACTIVE,
        category,
        condition,
        minPrice,
        maxPrice,
        search,
        pageable
    );

    List<ListingSummaryResponse> content = listingsPage.getContent().stream()
        .map(listing -> {
          Boolean isSaved = currentUserId != null
              && savedItemRepository.existsByUserIdAndListingId(currentUserId, listing.getId());
          return ListingSummaryResponse.fromEntity(listing, isSaved);
        })
        .toList();

    return PagedResponse.from(listingsPage, content);
  }

  /**
   * Get listings by user
   */
  @Transactional(readOnly = true)
  public List<ListingSummaryResponse> getListingsByUser(Long userId, Long currentUserId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User", "id", userId);
    }

    List<Listing> listings = listingRepository.findByUserIdAndStatusNot(userId, ListingStatus.DELETED);

    return listings.stream()
        .map(listing -> {
          Boolean isSaved = currentUserId != null
              && savedItemRepository.existsByUserIdAndListingId(currentUserId, listing.getId());
          return ListingSummaryResponse.fromEntity(listing, isSaved);
        })
        .toList();
  }

  /**
   * Update listing (owner only)
   */
  @Transactional
  public ListingResponse updateListing(Long listingId, ListingRequest request, Long userId) {
    Listing listing = getListingById(listingId);

    if (!listing.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only edit your own listings");
    }

    listing.setTitle(request.getTitle().trim());
    listing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
    listing.setPrice(request.getPrice());
    listing.setCategory(request.getCategory());
    listing.setCondition(request.getCondition());

    Listing updated = listingRepository.save(listing);
    log.info("Listing updated: {} by user {}", updated.getSlug(), userId);

    Boolean isSaved = savedItemRepository.existsByUserIdAndListingId(userId, listing.getId());
    return ListingResponse.fromEntity(updated, isSaved);
  }

  /**
   * Delete listing (owner only) - soft delete
   */
  @Transactional
  public void deleteListing(Long listingId, Long userId) {
    Listing listing = getListingById(listingId);

    if (!listing.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only delete your own listings");
    }

    listing.setStatus(ListingStatus.DELETED);
    listingRepository.save(listing);
    log.info("Listing deleted: {} by user {}", listing.getSlug(), userId);
  }

  /**
   * Mark listing as sold (owner only)
   */
  @Transactional
  public ListingResponse markAsSold(Long listingId, Long userId) {
    Listing listing = getListingById(listingId);

    if (!listing.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only update your own listings");
    }

    listing.setStatus(ListingStatus.SOLD);
    Listing updated = listingRepository.save(listing);
    log.info("Listing marked as sold: {} by user {}", listing.getSlug(), userId);

    Boolean isSaved = savedItemRepository.existsByUserIdAndListingId(userId, listing.getId());
    return ListingResponse.fromEntity(updated, isSaved);
  }

  /**
   * Bump listing (renew expiry - owner only)
   */
  @Transactional
  public ListingResponse bumpListing(Long listingId, Long userId) {
    Listing listing = getListingById(listingId);

    if (!listing.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You can only bump your own listings");
    }

    listing.setCreatedAt(LocalDateTime.now());
    listing.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
    listing.setStatus(ListingStatus.ACTIVE);

    Listing updated = listingRepository.save(listing);
    log.info("Listing bumped: {} by user {}", listing.getSlug(), userId);

    Boolean isSaved = savedItemRepository.existsByUserIdAndListingId(userId, listing.getId());
    return ListingResponse.fromEntity(updated, isSaved);
  }

  private String generateSlug(String title) {
    // 1. Process the string first
    String cleaned = title.toLowerCase()
        .replaceAll("[^a-z0-9\\s]", "")
        .replaceAll("\\s+", "-");

    // 2. Use the length of the 'cleaned' string for the safe substring
    String base = cleaned.substring(0, Math.min(cleaned.length(), 40));

    String randomSuffix = RandomStringUtils.randomAlphanumeric(6).toLowerCase();
    String slug = base + "-" + randomSuffix;

    while (listingRepository.existsBySlug(slug)) {
      randomSuffix = RandomStringUtils.randomAlphanumeric(6).toLowerCase();
      slug = base + "-" + randomSuffix;
    }

    return slug;
  }
}