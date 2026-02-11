package com.neumarket.service;

import com.neumarket.enums.ListingStatus;
import com.neumarket.repository.ListingRepository;
import com.neumarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

  private final UserRepository userRepository;
  private final ListingRepository listingRepository;

  @Value("${app.cleanup.unverified-user-expiry-hours:24}")
  private int unverifiedUserExpiryHours;

  /**
   * Delete unverified users who signed up more than 24 hours ago
   * Runs every 30 minutes
   */
  @Scheduled(fixedRate = 1800000) // 30 minutes in milliseconds
  @Transactional
  public void cleanupUnverifiedUsers() {
    LocalDateTime cutoffTime = LocalDateTime.now().minusHours(unverifiedUserExpiryHours);

    int deletedCount = userRepository.deleteUnverifiedUsersOlderThan(cutoffTime);

    if (deletedCount > 0) {
      log.info("Cleanup: Deleted {} unverified users older than {} hours",
          deletedCount, unverifiedUserExpiryHours);
    }
  }

  /**
   * Clear expired verification codes (keep user, just clear the code)
   * Runs every hour
   */
  @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
  @Transactional
  public void cleanupExpiredCodes() {
    LocalDateTime now = LocalDateTime.now();

    int verificationCodes = userRepository.clearExpiredVerificationCodes(now);
    int resetCodes = userRepository.clearExpiredPasswordResetCodes(now);

    if (verificationCodes > 0 || resetCodes > 0) {
      log.info("Cleanup: Cleared {} expired verification codes, {} expired reset codes",
          verificationCodes, resetCodes);
    }
  }

  /**
   * Mark expired listings as EXPIRED
   * Runs daily at 2 AM
   */
  @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM every day
  @Transactional
  public void cleanupExpiredListings() {
    LocalDateTime now = LocalDateTime.now();

    int expiredCount = listingRepository.markExpiredListings(now);

    if (expiredCount > 0) {
      log.info("Cleanup: Marked {} listings as expired", expiredCount);
    }
  }

  /**
   * Permanently delete listings that have been in DELETED status for more than 30 days
   * Runs daily at 3 AM
   */
  @Scheduled(cron = "0 0 0 1 * ?") // 3:00 AM every day
  @Transactional
  public void purgeDeletedListings() {
    LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);

    int purgedCount = listingRepository.purgeOldDeletedListings(cutoffTime);

    if (purgedCount > 0) {
      log.info("Cleanup: Permanently deleted {} old listings", purgedCount);
    }
  }
}