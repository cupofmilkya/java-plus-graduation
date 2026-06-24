package ru.practicum.common.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new event - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    
    @NotBlank(message = "Title must not be blank")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
    
    @NotBlank(message = "Annotation must not be blank")
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;
    
    @NotBlank(message = "Description must not be blank")
    @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters")
    private String description;
    
    @NotNull(message = "Category must not be null")
    private Long category;
    
    @NotNull(message = "Event date must not be null")
    private String eventDate;
    
    @NotNull(message = "Location must not be null")
    private LocationDto location;
    
    @Builder.Default
    private Boolean paid = false;
    
    @Builder.Default
    private Boolean requestModeration = true;
    
    @Builder.Default
    private Integer participantLimit = 0;
}
