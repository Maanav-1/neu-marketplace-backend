package com.neumarket.controller;

import com.neumarket.dto.request.ReportRequest;
import com.neumarket.dto.response.ReportResponse;
import com.neumarket.security.CurrentUser;
import com.neumarket.security.UserPrincipal;
import com.neumarket.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  /**
   * Create a report (user reports content)
   * POST /api/reports
   */
  @PostMapping
  public ResponseEntity<ReportResponse> createReport(
      @Valid @RequestBody ReportRequest request,
      @CurrentUser UserPrincipal currentUser) {

    ReportResponse response = reportService.createReport(request, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}