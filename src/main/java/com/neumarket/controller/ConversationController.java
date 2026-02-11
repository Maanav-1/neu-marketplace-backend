package com.neumarket.controller;

import com.neumarket.dto.request.MessageRequest;
import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.ChatMessageResponse;
import com.neumarket.dto.response.ConversationResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.ConversationService;
import com.neumarket.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationService conversationService;
  private final MessageService messageService;

  /**
   * Get all conversations for current user (inbox)
   * GET /api/conversations
   */
  @GetMapping
  public ResponseEntity<List<ConversationResponse>> getMyConversations(@CurrentUser UserPrincipal currentUser) {
    List<ConversationResponse> conversations = conversationService.getUserConversations(currentUser.getId());
    return ResponseEntity.ok(conversations);
  }

  /**
   * Start or get existing conversation for a listing
   * POST /api/conversations?listingId=123
   */
  @PostMapping
  public ResponseEntity<ConversationResponse> startConversation(
      @RequestParam Long listingId,
      @CurrentUser UserPrincipal currentUser) {

    ConversationResponse response = conversationService.getOrCreateConversation(listingId, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get a specific conversation
   * GET /api/conversations/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<ConversationResponse> getConversation(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    ConversationResponse response = conversationService.getConversation(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Get messages in a conversation
   * GET /api/conversations/{id}/messages
   */
  @GetMapping("/{id}/messages")
  public ResponseEntity<List<ChatMessageResponse>> getMessages(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    List<ChatMessageResponse> messages = messageService.getMessages(id, currentUser.getId());
    return ResponseEntity.ok(messages);
  }

  /**
   * Send a message in a conversation
   * POST /api/conversations/{id}/messages
   */
  @PostMapping("/{id}/messages")
  public ResponseEntity<ChatMessageResponse> sendMessage(
      @PathVariable Long id,
      @Valid @RequestBody MessageRequest request,
      @CurrentUser UserPrincipal currentUser) {

    ChatMessageResponse response = messageService.sendMessage(id, request, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Mark conversation as read
   * PATCH /api/conversations/{id}/read
   */
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse> markAsRead(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    messageService.markAsRead(id, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Messages marked as read"));
  }

  /**
   * Get unread count for a conversation
   * GET /api/conversations/{id}/unread
   */
  @GetMapping("/{id}/unread")
  public ResponseEntity<Map<String, Integer>> getUnreadCount(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    int count = messageService.getUnreadCount(id, currentUser.getId());
    return ResponseEntity.ok(Map.of("unreadCount", count));
  }

  /**
   * Get all conversations for a specific listing (seller only)
   * GET /api/conversations/listing/{listingId}
   */
  @GetMapping("/listing/{listingId}")
  public ResponseEntity<List<ConversationResponse>> getConversationsForListing(
      @PathVariable Long listingId,
      @CurrentUser UserPrincipal currentUser) {

    List<ConversationResponse> conversations =
        conversationService.getConversationsForListing(listingId, currentUser.getId());
    return ResponseEntity.ok(conversations);
  }

  @GetMapping("/total-unread")
  public ResponseEntity<Map<String, Integer>> getTotalUnreadCount(@CurrentUser UserPrincipal currentUser) {
    int count = messageService.getTotalUnreadCount(currentUser.getId());
    return ResponseEntity.ok(Map.of("totalUnread", count)); //
  }
}