package com.neumarket.service;

import com.neumarket.dto.request.ReportRequest;
import com.neumarket.dto.response.ApiResponse;
import com.neumarket.dto.response.ReportResponse;
import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import com.neumarket.exception.BadRequestException;
import com.neumarket.exception.ResourceNotFoundException;
import com.neumarket.model.Report;
import com.neumarket.model.User;
import com.neumarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final ListingRepository listingRepository;
  private final MessageRepository messageRepository;

  /**
   * Create a new report (user action)
   */
  @Transactional
  public ReportResponse createReport(ReportRequest request, Long reporterId) {
    User reporter = userRepository.findById(reporterId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

    // Validate target exists
    validateTargetExists(request.getReportType(), request.getTargetId());

    // Check if already reported
    if (reportRepository.existsByReporterIdAndReportTypeAndTargetId(
        reporterId, request.getReportType(), request.getTargetId())) {
      throw new BadRequestException("You have already reported this content");
    }

    // Can't report yourself
    if (request.getReportType() == ReportType.USER && request.getTargetId().equals(reporterId)) {
      throw new BadRequestException("You cannot report yourself");
    }

    Report report = Report.builder()
        .reporter(reporter)
        .reportType(request.getReportType())
        .targetId(request.getTargetId())
        .reason(request.getReason())
        .description(request.getDescription())
        .status(ReportStatus.PENDING)
        .build();

    Report saved = reportRepository.save(report);
    log.info("Report created: {} reported {} #{} for {}",
        reporterId, request.getReportType(), request.getTargetId(), request.getReason());

    return ReportResponse.fromEntity(saved);
  }

  /**
   * Validate that the reported content exists
   */
  private void validateTargetExists(ReportType type, Long targetId) {
    boolean exists = switch (type) {
      case LISTING -> listingRepository.existsById(targetId);
      case USER -> userRepository.existsById(targetId);
      case MESSAGE -> messageRepository.existsById(targetId);
    };

    if (!exists) {
      throw new ResourceNotFoundException(type.name(), "id", targetId);
    }
  }
}