package com.neumarket.service;

import com.neumarket.dto.response.ConversationResponse;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ForbiddenException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Conversation;
import com.neumarket.model.Listing;
import com.neumarket.model.User;
import com.neumarket.repository.ConversationRepository;
import com.neumarket.repository.ListingRepository;
import com.neumarket.repository.MessageRepository;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

  private final ConversationRepository conversationRepository;
  private final ListingRepository listingRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  /**
   * Get all conversations for current user (inbox)
   */
  @Transactional(readOnly = true)
  public List<ConversationResponse> getUserConversations(Long userId) {
    List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);

    return conversations.stream()
        .map(conv -> {
          int unreadCount = messageRepository.countUnreadMessages(conv.getId(), userId);
          return ConversationResponse.fromEntity(conv, userId, unreadCount);
        })
        .toList();
  }

  /**
   * Get or create a conversation for a listing
   * - If buyer already has conversation about this listing, return it
   * - Otherwise create new conversation
   */
  @Transactional
  public ConversationResponse getOrCreateConversation(Long listingId, Long buyerId) {
    User buyer = userRepository.findById(buyerId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

    // Restriction: User must be verified to start a conversation
    if (!buyer.getEmailVerified()) {
      throw new BadRequestException("Please verify your email before chatting with sellers.");
    }

    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

    // Can't message yourself
    if (listing.getUser().getId().equals(buyerId)) {
      throw new BadRequestException("You cannot start a conversation on your own listing");
    }

    // Check if conversation already exists
    Optional<Conversation> existing = conversationRepository.findByListingIdAndBuyerId(listingId, buyerId);

    if (existing.isPresent()) {
      Conversation conv = existing.get();
      int unreadCount = messageRepository.countUnreadMessages(conv.getId(), buyerId);
      return ConversationResponse.fromEntity(conv, buyerId, unreadCount);
    }

    Conversation conversation = Conversation.builder()
        .listing(listing)
        .buyer(buyer)
        .seller(listing.getUser())
        .build();

    Conversation saved = conversationRepository.save(conversation);
    log.info("Conversation created: {} between buyer {} and seller {} for listing {}",
        saved.getId(), buyerId, listing.getUser().getId(), listingId);

    return ConversationResponse.fromEntity(saved, buyerId, 0);
  }

  /**
   * Get conversation by ID (with access check)
   */
  @Transactional(readOnly = true)
  public ConversationResponse getConversation(Long conversationId, Long userId) {
    Conversation conversation = getConversationWithAccessCheck(conversationId, userId);
    int unreadCount = messageRepository.countUnreadMessages(conversationId, userId);
    return ConversationResponse.fromEntity(conversation, userId, unreadCount);
  }

  /**
   * Get conversation entity with access check
   */
  @Transactional(readOnly = true)
  public Conversation getConversationWithAccessCheck(Long conversationId, Long userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

    // Check if user is participant
    if (!conversation.isParticipant(userId)) {
      throw new ForbiddenException("You are not a participant in this conversation");
    }

    return conversation;
  }

  /**
   * Get conversations for a specific listing (for seller to see all inquiries)
   */
  @Transactional(readOnly = true)
  public List<ConversationResponse> getConversationsForListing(Long listingId, Long sellerId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

    // Only seller can see all conversations for their listing
    if (!listing.getUser().getId().equals(sellerId)) {
      throw new ForbiddenException("You can only view conversations for your own listings");
    }

    List<Conversation> conversations = conversationRepository.findByListingId(listingId);

    return conversations.stream()
        .map(conv -> {
          int unreadCount = messageRepository.countUnreadMessages(conv.getId(), sellerId);
          return ConversationResponse.fromEntity(conv, sellerId, unreadCount);
        })
        .toList();
  }
}