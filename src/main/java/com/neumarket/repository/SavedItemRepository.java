package com.neumarket.repository;

import com.neumarket.model.SavedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {

  List<SavedItem> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<SavedItem> findByUserIdAndListingId(Long userId, Long listingId);

  boolean existsByUserIdAndListingId(Long userId, Long listingId);

  void deleteByUserIdAndListingId(Long userId, Long listingId);
}