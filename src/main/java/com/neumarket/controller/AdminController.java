package com.neumarket.controller;

import com.neumarket.dto.request.BlockUserRequest;
import com.neumarket.dto.request.ReviewReportRequest;
import com.neumarket.dto.response.*;
import com.neumarket.enums.ListingStatus;
import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // All endpoints require ADMIN role
public class AdminController {

  private final AdminService adminService;

  // ==================== DASHBOARD ====================

  /**
   * Get dashboard statistics
   * GET /api/admin/dashboard
   */
  @GetMapping("/dashboard")
  public ResponseEntity<DashboardStatsResponse> getDashboard() {
    DashboardStatsResponse stats = adminService.getDashboardStats();
    return ResponseEntity.ok(stats);
  }

  // ==================== USER MANAGEMENT ====================

  /**
   * Get all users
   * GET /api/admin/users?search=john&page=0&size=20
   */
  @GetMapping("/users")
  public ResponseEntity<PagedResponse<AdminUserResponse>> getAllUsers(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    PagedResponse<AdminUserResponse> response = adminService.getAllUsers(search, page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * Get user details
   * GET /api/admin/users/{id}
   */
  @GetMapping("/users/{id}")
  public ResponseEntity<AdminUserResponse> getUserDetails(@PathVariable Long id) {
    AdminUserResponse response = adminService.getUserDetails(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Block a user
   * POST /api/admin/users/{id}/block
   */
  @PostMapping("/users/{id}/block")
  public ResponseEntity<AdminUserResponse> blockUser(
      @PathVariable Long id,
      @Valid @RequestBody BlockUserRequest request,
      @CurrentUser UserPrincipal currentUser) {

    AdminUserResponse response = adminService.blockUser(id, request, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Unblock a user
   * POST /api/admin/users/{id}/unblock
   */
  @PostMapping("/users/{id}/unblock")
  public ResponseEntity<AdminUserResponse> unblockUser(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    AdminUserResponse response = adminService.unblockUser(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Make user an admin
   * POST /api/admin/users/{id}/make-admin
   */
  @PostMapping("/users/{id}/make-admin")
  public ResponseEntity<AdminUserResponse> makeAdmin(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    AdminUserResponse response = adminService.makeAdmin(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  /**
   * Remove admin role from user
   * POST /api/admin/users/{id}/remove-admin
   */
  @PostMapping("/users/{id}/remove-admin")
  public ResponseEntity<AdminUserResponse> removeAdmin(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    AdminUserResponse response = adminService.removeAdmin(id, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  // ==================== LISTING MANAGEMENT ====================

  /**
   * Get all listings (admin view)
   * GET /api/admin/listings?status=ACTIVE&search=iphone&page=0&size=20
   */
  @GetMapping("/listings")
  public ResponseEntity<PagedResponse<ListingSummaryResponse>> getAllListings(
      @RequestParam(required = false) ListingStatus status,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    PagedResponse<ListingSummaryResponse> response = adminService.getAllListings(status, search, page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * Admin delete a listing
   * DELETE /api/admin/listings/{id}
   */
  @DeleteMapping("/listings/{id}")
  public ResponseEntity<ApiResponse> deleteListing(
      @PathVariable Long id,
      @CurrentUser UserPrincipal currentUser) {

    adminService.adminDeleteListing(id, currentUser.getId());
    return ResponseEntity.ok(ApiResponse.success("Listing deleted by admin"));
  }

  // ==================== REPORT MANAGEMENT ====================

  /**
   * Get all reports
   * GET /api/admin/reports?status=PENDING&type=LISTING&page=0&size=20
   */
  @GetMapping("/reports")
  public ResponseEntity<PagedResponse<ReportResponse>> getReports(
      @RequestParam(required = false) ReportStatus status,
      @RequestParam(required = false) ReportType type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    PagedResponse<ReportResponse> response = adminService.getReports(status, type, page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * Get single report
   * GET /api/admin/reports/{id}
   */
  @GetMapping("/reports/{id}")
  public ResponseEntity<ReportResponse> getReport(@PathVariable Long id) {
    ReportResponse response = adminService.getReport(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Review a report
   * PATCH /api/admin/reports/{id}/review
   */
  @PatchMapping("/reports/{id}/review")
  public ResponseEntity<ReportResponse> reviewReport(
      @PathVariable Long id,
      @Valid @RequestBody ReviewReportRequest request,
      @CurrentUser UserPrincipal currentUser) {

    ReportResponse response = adminService.reviewReport(id, request, currentUser.getId());
    return ResponseEntity.ok(response);
  }

  // ==================== CONVERSATION OVERSIGHT ====================

  /**
   * Get all conversations (admin oversight)
   * GET /api/admin/conversations?page=0&size=20
   */
  @GetMapping("/conversations")
  public ResponseEntity<PagedResponse<ConversationResponse>> getAllConversations(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    PagedResponse<ConversationResponse> response = adminService.getAllConversations(page, size);
    return ResponseEntity.ok(response);
  }

  /**
   * Get messages in a conversation (admin oversight)
   * GET /api/admin/conversations/{id}/messages
   */
  @GetMapping("/conversations/{id}/messages")
  public ResponseEntity<List<ChatMessageResponse>> getConversationMessages(@PathVariable Long id) {
    List<ChatMessageResponse> messages = adminService.getConversationMessages(id);
    return ResponseEntity.ok(messages);
  }
}