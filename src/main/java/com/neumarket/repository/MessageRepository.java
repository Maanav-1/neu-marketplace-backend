package com.neumarket.repository;

import com.neumarket.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

  List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

  @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC LIMIT 1")
  Optional<Message> findLastMessageByConversationId(@Param("conversationId") Long conversationId);

  @Query("SELECT COUNT(m) FROM Message m " +
      "WHERE m.conversation.id = :conversationId " +
      "AND m.sender.id != :userId " +
      "AND m.isRead = false")
  int countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

  @Modifying
  @Query("UPDATE Message m SET m.isRead = true " +
      "WHERE m.conversation.id = :conversationId " +
      "AND m.sender.id != :userId " +
      "AND m.isRead = false")
  void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

  @Query("SELECT COUNT(m) FROM Message m " +
      "WHERE (m.conversation.buyer.id = :userId OR m.conversation.seller.id = :userId) " +
      "AND m.sender.id != :userId " +
      "AND m.isRead = false")
  int countTotalUnreadMessages(@Param("userId") Long userId); //
}