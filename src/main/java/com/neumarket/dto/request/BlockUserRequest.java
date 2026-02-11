package com.neumarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequest {

  @NotBlank(message = "Block reason is required")
  @Size(max = 500, message = "Reason cannot exceed 500 characters")
  private String reason;
}