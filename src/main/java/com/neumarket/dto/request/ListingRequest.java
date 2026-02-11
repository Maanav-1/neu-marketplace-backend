package com.neumarket.dto.request;

import com.neumarket.enums.Category;
import com.neumarket.enums.Condition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingRequest {

  @NotBlank(message = "Title is required")
  @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
  private String title;

  @Size(max = 2000, message = "Description cannot exceed 2000 characters")
  private String description;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.00", message = "Price cannot be negative")
  @DecimalMax(value = "99999.99", message = "Price cannot exceed $99,999.99")
  private BigDecimal price;

  @NotNull(message = "Category is required")
  private Category category;

  @NotNull(message = "Condition is required")
  private Condition condition;
}