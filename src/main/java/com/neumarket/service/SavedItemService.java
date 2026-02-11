package com.neumarket.service;

import com.neumarket.dto.response.SavedItemResponse;
import com.neumarket.enums.ListingStatus;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Listing;
import com.neumarket.model.SavedItem;
import com.neumarket.model.User;
import com.neumarket.repository.ListingRepository;
import com.neumarket.repository.SavedItemRepository;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedItemService {

  private final SavedItemRepository savedItemRepository;
  private final ListingRepository listingRepository;
  private final UserRepository userRepository;

  /**
   * Get all saved items for current user
   */
  @Transactional(readOnly = true)
  public List<SavedItemResponse> getSavedItems(Long userId) {
    List<SavedItem> savedItems = savedItemRepository.findByUserIdOrderByCreatedAtDesc(userId);

    return savedItems.stream()
        .filter(saved -> saved.getListing().getStatus() != ListingStatus.DELETED) // Filter out deleted items
        .map(SavedItemResponse::fromEntity)
        .toList();
  }

  /**
   * Save a listing
   */
  @Transactional
  public SavedItemResponse saveListing(Long listingId, Long userId) {
    // Check if listing exists
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

    // Can't save your own listing
    if (listing.getUser().getId().equals(userId)) {
      throw new BadRequestException("You cannot save your own listing");
    }

    // Check if already saved
    if (savedItemRepository.existsByUserIdAndListingId(userId, listingId)) {
      throw new BadRequestException("Listing already saved");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    SavedItem savedItem = SavedItem.builder()
        .user(user)
        .listing(listing)
        .build();

    SavedItem saved = savedItemRepository.save(savedItem);
    log.info("User {} saved listing {}", userId, listingId);

    return SavedItemResponse.fromEntity(saved);
  }

  /**
   * Unsave a listing
   */
  @Transactional
  public void unsaveListing(Long listingId, Long userId) {
    SavedItem savedItem = savedItemRepository.findByUserIdAndListingId(userId, listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Saved item not found"));

    savedItemRepository.delete(savedItem);
    log.info("User {} unsaved listing {}", userId, listingId);
  }

  /**
   * Check if listing is saved by user
   */
  @Transactional(readOnly = true)
  public boolean isListingSaved(Long listingId, Long userId) {
    return savedItemRepository.existsByUserIdAndListingId(userId, listingId);
  }
}