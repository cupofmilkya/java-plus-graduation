package ru.practicum.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new category - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {
    
    @NotBlank(message = "Category name must not be blank")
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    private String name;
}
