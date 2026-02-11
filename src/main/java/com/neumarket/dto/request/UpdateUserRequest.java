package com.neumarket.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @Size(max = 20, message = "Phone cannot exceed 20 characters")
  private String phone;
}