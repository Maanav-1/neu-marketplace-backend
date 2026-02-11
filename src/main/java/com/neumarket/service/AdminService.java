package com.neumarket.service;

import com.neumarket.dto.request.BlockUserRequest;
import com.neumarket.dto.request.ReviewReportRequest;
import com.neumarket.dto.response.*;
import com.neumarket.enums.ListingStatus;
import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import com.neumarket.enums.Role;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Listing;
import com.neumarket.model.Report;
import com.neumarket.model.User;
import com.neumarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

  private final UserRepository userRepository;
  private final ListingRepository listingRepository;
  private final ReportRepository reportRepository;
  private final ConversationRepository conversationRepository;
  private final MessageRepository messageRepository;
  private final MessageService messageService;

  // ==================== USER MANAGEMENT ====================

  @Transactional(readOnly = true)
  public PagedResponse<AdminUserResponse> getAllUsers(String search, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<User> userPage;
    if (search != null && !search.isBlank()) {
      userPage = userRepository.searchUsers(search.trim(), pageable);
    } else {
      // FIX: Use standard findAll(pageable) instead of findAllByOrderByCreatedAtDesc
      userPage = userRepository.findAll(pageable);
    }

    List<AdminUserResponse> content = userPage.getContent().stream()
        .map(AdminUserResponse::fromEntity)
        .toList();

    return PagedResponse.from(userPage, content);
  }

  @Transactional(readOnly = true)
  public AdminUserResponse getUserDetails(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    int listingsCount = listingRepository.findByUserIdAndStatusNot(userId, ListingStatus.DELETED).size();
    int conversationsCount = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).size();

    return AdminUserResponse.fromEntity(user, listingsCount, conversationsCount);
  }

  @Transactional
  public AdminUserResponse blockUser(Long userId, BlockUserRequest request, Long adminId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (user.getRole() == Role.ADMIN) {
      throw new BadRequestException("Cannot block an admin user");
    }

    if (user.getBlocked()) {
      throw new BadRequestException("User is already blocked");
    }

    user.setBlocked(true);
    user.setBlockReason(request.getReason());
    user.setBlockedAt(LocalDateTime.now());

    User saved = userRepository.save(user);
    log.info("User {} blocked by admin {} for: {}", userId, adminId, request.getReason());

    return AdminUserResponse.fromEntity(saved);
  }

  @Transactional
  public AdminUserResponse unblockUser(Long userId, Long adminId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (!user.getBlocked()) {
      throw new BadRequestException("User is not blocked");
    }

    user.setBlocked(false);
    user.setBlockReason(null);
    user.setBlockedAt(null);

    User saved = userRepository.save(user);
    log.info("User {} unblocked by admin {}", userId, adminId);

    return AdminUserResponse.fromEntity(saved);
  }

  @Transactional
  public AdminUserResponse makeAdmin(Long userId, Long adminId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (user.getRole() == Role.ADMIN) {
      throw new BadRequestException("User is already an admin");
    }

    if (!user.getEmailVerified()) {
      throw new BadRequestException("Cannot make unverified user an admin");
    }

    user.setRole(Role.ADMIN);
    User saved = userRepository.save(user);
    log.info("User {} promoted to admin by admin {}", userId, adminId);

    return AdminUserResponse.fromEntity(saved);
  }

  @Transactional
  public AdminUserResponse removeAdmin(Long userId, Long adminId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (user.getRole() != Role.ADMIN) {
      throw new BadRequestException("User is not an admin");
    }

    if (userId.equals(adminId)) {
      throw new BadRequestException("Cannot remove your own admin role");
    }

    user.setRole(Role.USER);
    User saved = userRepository.save(user);
    log.info("User {} demoted from admin by admin {}", userId, adminId);

    return AdminUserResponse.fromEntity(saved);
  }

  // ==================== LISTING MANAGEMENT ====================

  @Transactional(readOnly = true)
  public PagedResponse<ListingSummaryResponse> getAllListings(ListingStatus status, String search, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<Listing> listingPage;
    if (search != null && !search.isBlank()) {
      listingPage = listingRepository.searchListings(search.trim(), pageable);
    } else if (status != null) {
      listingPage = listingRepository.findByStatus(status, pageable);
    } else {
      // FIX: Use standard findAll(pageable) instead of findAllByOrderByCreatedAtDesc
      listingPage = listingRepository.findAll(pageable);
    }

    List<ListingSummaryResponse> content = listingPage.getContent().stream()
        .map(listing -> ListingSummaryResponse.fromEntity(listing, false))
        .toList();

    return PagedResponse.from(listingPage, content);
  }

  @Transactional
  public void adminDeleteListing(Long listingId, Long adminId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));

    listing.setStatus(ListingStatus.DELETED);
    listingRepository.save(listing);
    log.info("Listing {} deleted by admin {}", listingId, adminId);
  }

  // ==================== REPORT MANAGEMENT ====================

  @Transactional(readOnly = true)
  public PagedResponse<ReportResponse> getReports(ReportStatus status, ReportType type, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    Page<Report> reportPage;
    if (status != null && type != null) {
      reportPage = reportRepository.findByStatusAndReportType(status, type, pageable);
    } else if (status != null) {
      reportPage = reportRepository.findByStatus(status, pageable);
    } else if (type != null) {
      reportPage = reportRepository.findByReportType(type, pageable);
    } else {
      // FIX: Use standard findAll(pageable) instead of findAllByOrderByCreatedAtDesc
      reportPage = reportRepository.findAll(pageable);
    }

    List<ReportResponse> content = reportPage.getContent().stream()
        .map(ReportResponse::fromEntity)
        .toList();

    return PagedResponse.from(reportPage, content);
  }

  @Transactional(readOnly = true)
  public ReportResponse getReport(Long reportId) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
    return ReportResponse.fromEntity(report);
  }

  @Transactional
  public ReportResponse reviewReport(Long reportId, ReviewReportRequest request, Long adminId) {
    Report report = reportRepository.findById(reportId)
        .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

    User admin = userRepository.findById(adminId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

    report.setStatus(request.getStatus());
    report.setAdminNotes(request.getAdminNotes());
    report.setReviewedBy(admin);
    report.setReviewedAt(LocalDateTime.now());

    Report saved = reportRepository.save(report);
    log.info("Report {} reviewed by admin {} with status {}", reportId, adminId, request.getStatus());

    return ReportResponse.fromEntity(saved);
  }

  // ==================== CONVERSATIONS (Oversight) ====================

  @Transactional(readOnly = true)
  public PagedResponse<ConversationResponse> getAllConversations(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
    Page<com.neumarket.model.Conversation> convPage = conversationRepository.findAll(pageable);

    List<ConversationResponse> content = convPage.getContent().stream()
        .map(conv -> ConversationResponse.fromEntity(conv, conv.getSeller().getId(), 0))
        .toList();

    return PagedResponse.from(convPage, content);
  }

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getConversationMessages(Long conversationId) {
    return messageService.getMessagesForAdmin(conversationId);
  }

  // ==================== DASHBOARD STATS ====================

  @Transactional(readOnly = true)
  public DashboardStatsResponse getDashboardStats() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime startOfWeek = now.minusDays(7);
    LocalDateTime startOfMonth = now.minusDays(30);

    long totalUsers = userRepository.count();
    long verifiedUsers = userRepository.countByEmailVerifiedTrue();
    long blockedUsers = userRepository.countByBlockedTrue();
    long newUsersToday = userRepository.countNewUsersSince(startOfToday);
    long newUsersThisWeek = userRepository.countNewUsersSince(startOfWeek);
    long newUsersThisMonth = userRepository.countNewUsersSince(startOfMonth);

    long totalListings = listingRepository.count();
    long activeListings = listingRepository.countByStatus(ListingStatus.ACTIVE);
    long soldListings = listingRepository.countByStatus(ListingStatus.SOLD);
    long newListingsToday = listingRepository.countNewListingsSince(startOfToday);
    long newListingsThisWeek = listingRepository.countNewListingsSince(startOfWeek);
    BigDecimal averagePrice = listingRepository.getAveragePrice();

    Map<String, Long> listingsByCategory = new HashMap<>();
    listingRepository.countByCategory().forEach(row -> {
      listingsByCategory.put(row[0].toString(), (Long) row[1]);
    });

    long totalReports = reportRepository.count();
    long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
    long resolvedReports = reportRepository.countByStatus(ReportStatus.ACTION_TAKEN)
        + reportRepository.countByStatus(ReportStatus.DISMISSED);

    long totalConversations = conversationRepository.count();
    long totalMessages = messageRepository.count();

    return DashboardStatsResponse.builder()
        .totalUsers(totalUsers)
        .verifiedUsers(verifiedUsers)
        .blockedUsers(blockedUsers)
        .newUsersToday(newUsersToday)
        .newUsersThisWeek(newUsersThisWeek)
        .newUsersThisMonth(newUsersThisMonth)
        .totalListings(totalListings)
        .activeListings(activeListings)
        .soldListings(soldListings)
        .newListingsToday(newListingsToday)
        .newListingsThisWeek(newListingsThisWeek)
        .averageListingPrice(averagePrice != null ? averagePrice : BigDecimal.ZERO)
        .listingsByCategory(listingsByCategory)
        .totalReports(totalReports)
        .pendingReports(pendingReports)
        .resolvedReports(resolvedReports)
        .totalConversations(totalConversations)
        .totalMessages(totalMessages)
        .build();
  }
}