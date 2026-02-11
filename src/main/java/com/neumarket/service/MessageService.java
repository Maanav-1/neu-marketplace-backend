package com.neumarket.service;

import com.neumarket.dto.request.MessageRequest;
import com.neumarket.dto.response.ChatMessageResponse;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ForbiddenException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Conversation;
import com.neumarket.model.Message;
import com.neumarket.model.User;
import com.neumarket.repository.ConversationRepository;
import com.neumarket.repository.MessageRepository;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

  private final MessageRepository messageRepository;
  private final ConversationRepository conversationRepository;
  private final UserRepository userRepository;

  /**
   * Get all messages in a conversation
   */
  @Transactional
  public List<ChatMessageResponse> getMessages(Long conversationId, Long userId) {
    Conversation conversation = getConversationWithAccessCheck(conversationId, userId);

    // Mark messages as read (messages sent by the OTHER person)
    messageRepository.markMessagesAsRead(conversationId, userId);

    // Get all messages
    List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

    return messages.stream()
        .map(msg -> ChatMessageResponse.fromEntity(msg, userId))
        .toList();
  }

  /**
   * Send a message in a conversation
   */
  @Transactional
  public ChatMessageResponse sendMessage(Long conversationId, MessageRequest request, Long senderId) {
    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

    // Restriction: User must be verified to send messages
    if (!sender.getEmailVerified()) {
      throw new BadRequestException("Verification required to send messages.");
    }

    Conversation conversation = getConversationWithAccessCheck(conversationId, senderId);

    // Create message
    Message message = Message.builder()
        .conversation(conversation)
        .sender(sender)
        .content(request.getContent().trim())
        .isRead(false)
        .build();

    Message saved = messageRepository.save(message);

    // Update conversation's updatedAt timestamp
    conversation.setUpdatedAt(java.time.LocalDateTime.now());
    conversationRepository.save(conversation);

    log.info("Message sent in conversation {} by user {}", conversationId, senderId);

    return ChatMessageResponse.fromEntity(saved, senderId);
  }

  /**
   * Mark all messages in conversation as read
   */
  @Transactional
  public void markAsRead(Long conversationId, Long userId) {
    // Verify access
    getConversationWithAccessCheck(conversationId, userId);

    // Mark messages as read
    messageRepository.markMessagesAsRead(conversationId, userId);
    log.info("Messages marked as read in conversation {} for user {}", conversationId, userId);
  }

  /**
   * Get unread message count for a conversation
   */
  @Transactional(readOnly = true)
  public int getUnreadCount(Long conversationId, Long userId) {
    // Verify access
    getConversationWithAccessCheck(conversationId, userId);
    return messageRepository.countUnreadMessages(conversationId, userId);
  }

  /**
   * Helper: Get conversation with access check
   */
  private Conversation getConversationWithAccessCheck(Long conversationId, Long userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

    if (!conversation.isParticipant(userId)) {
      throw new ForbiddenException("You are not a participant in this conversation");
    }

    return conversation;
  }

  /**
   * Get all messages in a conversation for admin oversight (bypasses participant checks)
   */
  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessagesForAdmin(Long conversationId) {
    // Check if conversation exists
    if (!conversationRepository.existsById(conversationId)) {
      throw new ResourceNotFoundException("Conversation", "id", conversationId);
    }

    // Get all messages ordered by creation date
    List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

    // Map to response DTOs. Pass null for userId so that 'isOwnMessage' is false for all
    return messages.stream()
        .map(msg -> ChatMessageResponse.fromEntity(msg, null))
        .toList();
  }


  @Transactional(readOnly = true)
  public int getTotalUnreadCount(Long userId) {
    return messageRepository.countTotalUnreadMessages(userId); //
  }

}