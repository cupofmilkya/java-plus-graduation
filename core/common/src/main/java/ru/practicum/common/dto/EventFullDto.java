package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for full event information - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private UserShortDto initiator;
    private Boolean paid;
    private LocalDateTime createdOn;
    private LocalDateTime eventDate;
    private LocalDateTime publishedOn;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
    private Integer confirmedRequests;
    private LocationDto location;
    private Long views;
}
