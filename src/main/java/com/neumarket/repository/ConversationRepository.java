package com.neumarket.repository;

import com.neumarket.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

  Optional<Conversation> findByListingIdAndBuyerId(Long listingId, Long buyerId);

  @Query("SELECT c FROM Conversation c " +
      "WHERE c.buyer.id = :userId OR c.seller.id = :userId " +
      "ORDER BY c.updatedAt DESC")
  List<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

  @Query("SELECT c FROM Conversation c " +
      "WHERE c.listing.id = :listingId " +
      "ORDER BY c.updatedAt DESC")
  List<Conversation> findByListingId(@Param("listingId") Long listingId);
}