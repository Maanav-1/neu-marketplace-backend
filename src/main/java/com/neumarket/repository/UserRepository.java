package com.neumarket.repository;

import com.neumarket.enums.Role;
import com.neumarket.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);



  // Admin: Search users
  @Query("SELECT u FROM User u WHERE " +
      "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
      "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
  Page<User> searchUsers(@Param("search") String search, Pageable pageable);

  // Admin: Get blocked users
  Page<User> findByBlockedTrue(Pageable pageable);

  // Admin: Get users by role
  List<User> findByRole(Role role);

  // Admin: Count stats
  long countByEmailVerifiedTrue();

  long countByBlockedTrue();

  long countByRole(Role role);

  @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
  long countNewUsersSince(@Param("since") LocalDateTime since);

  // ==================== CLEANUP QUERIES ====================

  /**
   * Delete unverified users older than specified time
   * Returns number of deleted users
   */
  @Modifying
  @Query("DELETE FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffTime")
  int deleteUnverifiedUsersOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

  /**
   * Clear expired verification codes
   */
  @Modifying
  @Query("UPDATE User u SET u.verificationCode = null, u.verificationCodeExpiry = null " +
      "WHERE u.verificationCodeExpiry < :now AND u.verificationCode IS NOT NULL")
  int clearExpiredVerificationCodes(@Param("now") LocalDateTime now);

  /**
   * Clear expired password reset codes
   */
  @Modifying
  @Query("UPDATE User u SET u.passwordResetCode = null, u.passwordResetCodeExpiry = null " +
      "WHERE u.passwordResetCodeExpiry < :now AND u.passwordResetCode IS NOT NULL")
  int clearExpiredPasswordResetCodes(@Param("now") LocalDateTime now);

  /**
   * Count unverified users (for stats)
   */
  long countByEmailVerifiedFalse();
}