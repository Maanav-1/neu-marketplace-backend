package com.neumarket.dto.request;

import com.neumarket.enums.ReportReason;
import com.neumarket.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

  @NotNull(message = "Report type is required")
  private ReportType reportType;

  @NotNull(message = "Target ID is required")
  private Long targetId;

  @NotNull(message = "Reason is required")
  private ReportReason reason;

  @Size(max = 1000, message = "Description cannot exceed 1000 characters")
  private String description;
}