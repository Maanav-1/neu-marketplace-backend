package com.neumarket.dto.request;

import com.neumarket.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportRequest {

  @NotNull(message = "Status is required")
  private ReportStatus status;

  @Size(max = 1000, message = "Admin notes cannot exceed 1000 characters")
  private String adminNotes;
}