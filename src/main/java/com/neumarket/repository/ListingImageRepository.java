package com.neumarket.repository;

import com.neumarket.model.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

  List<ListingImage> findByListingIdOrderByDisplayOrderAsc(Long listingId);

  @Query("SELECT COUNT(i) FROM ListingImage i WHERE i.listing.id = :listingId")
  int countByListingId(@Param("listingId") Long listingId);

  @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM ListingImage i WHERE i.listing.id = :listingId")
  int findMaxDisplayOrderByListingId(@Param("listingId") Long listingId);
}