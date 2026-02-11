package com.neumarket.repository;

import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import com.neumarket.enums.ListingStatus;
import com.neumarket.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

  Optional<Listing> findBySlug(String slug);

  boolean existsBySlug(String slug);

  List<Listing> findByUserIdAndStatusNot(Long userId, ListingStatus status);

  @Query("SELECT l FROM Listing l WHERE l.status = :status " +
      "AND (:category IS NULL OR l.category = :category) " +
      "AND (:condition IS NULL OR l.condition = :condition) " +
      "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
      "AND (:search IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
      "     OR LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Listing> findWithFilters(
      @Param("status") ListingStatus status,
      @Param("category") Category category,
      @Param("condition") Condition condition,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("search") String search,
      Pageable pageable
  );



  // Admin: Get listings by status
  Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

  // Admin: Search listings
  @Query("SELECT l FROM Listing l WHERE " +
      "LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(l.description) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<Listing> searchListings(@Param("search") String search, Pageable pageable);

  // Admin: Count stats
  long countByStatus(ListingStatus status);

  @Query("SELECT COUNT(l) FROM Listing l WHERE l.createdAt >= :since")
  long countNewListingsSince(@Param("since") LocalDateTime since);

  @Query("SELECT l.category, COUNT(l) FROM Listing l WHERE l.status = 'ACTIVE' GROUP BY l.category")
  List<Object[]> countByCategory();

  @Query("SELECT AVG(l.price) FROM Listing l WHERE l.status = 'ACTIVE'")
  BigDecimal getAveragePrice();

  // ==================== CLEANUP QUERIES ====================

  /**
   * Mark expired listings as EXPIRED
   * Returns number of updated listings
   */
  @Modifying
  @Query("UPDATE Listing l SET l.status = 'EXPIRED' " +
      "WHERE l.status = 'ACTIVE' AND l.expiresAt < :now")
  int markExpiredListings(@Param("now") LocalDateTime now);

  /**
   * Permanently delete listings that have been DELETED for more than X days
   * Returns number of deleted listings
   */
  @Modifying
  @Query("DELETE FROM Listing l WHERE l.status = 'DELETED' AND l.updatedAt < :cutoffTime")
  int purgeOldDeletedListings(@Param("cutoffTime") LocalDateTime cutoffTime);
}