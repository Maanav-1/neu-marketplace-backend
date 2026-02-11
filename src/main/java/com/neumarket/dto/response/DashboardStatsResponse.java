package com.neumarket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

  // User stats
  private long totalUsers;
  private long verifiedUsers;
  private long blockedUsers;
  private long newUsersToday;
  private long newUsersThisWeek;
  private long newUsersThisMonth;

  // Listing stats
  private long totalListings;
  private long activeListings;
  private long soldListings;
  private long newListingsToday;
  private long newListingsThisWeek;
  private BigDecimal averageListingPrice;
  private Map<String, Long> listingsByCategory;

  // Report stats
  private long totalReports;
  private long pendingReports;
  private long resolvedReports;

  // Conversation stats
  private long totalConversations;
  private long totalMessages;
}