package com.neumarket.dto.response;

import com.neumarket.enums.ReportReason;
import com.neumarket.enums.ReportStatus;
import com.neumarket.enums.ReportType;
import com.neumarket.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

  private Long id;
  private ReporterInfo reporter;
  private ReportType reportType;
  private Long targetId;
  private ReportReason reason;
  private String reasonDisplayName;
  private String description;
  private ReportStatus status;
  private ReviewerInfo reviewedBy;
  private String adminNotes;
  private LocalDateTime reviewedAt;
  private LocalDateTime createdAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ReporterInfo {
    private Long id;
    private String name;
    private String email;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ReviewerInfo {
    private Long id;
    private String name;
  }

  public static ReportResponse fromEntity(Report report) {
    ReportResponseBuilder builder = ReportResponse.builder()
        .id(report.getId())
        .reporter(ReporterInfo.builder()
            .id(report.getReporter().getId())
            .name(report.getReporter().getName())
            .email(report.getReporter().getEmail())
            .build())
        .reportType(report.getReportType())
        .targetId(report.getTargetId())
        .reason(report.getReason())
        .reasonDisplayName(report.getReason().getDisplayName())
        .description(report.getDescription())
        .status(report.getStatus())
        .adminNotes(report.getAdminNotes())
        .reviewedAt(report.getReviewedAt())
        .createdAt(report.getCreatedAt());

    if (report.getReviewedBy() != null) {
      builder.reviewedBy(ReviewerInfo.builder()
          .id(report.getReviewedBy().getId())
          .name(report.getReviewedBy().getName())
          .build());
    }

    return builder.build();
  }
}