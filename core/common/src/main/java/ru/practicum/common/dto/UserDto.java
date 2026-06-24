package ru.practicum.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user information - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    
    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 250, message = "Name must be between 2 and 250 characters")
    private String name;
    
    @NotBlank(message = "Email must not be blank")
    @Size(min = 6, max = 254, message = "Email must be between 6 and 254 characters")
    private String email;
}
