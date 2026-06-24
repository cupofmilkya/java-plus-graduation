package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for short event information - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private UserShortDto initiator;
    private Boolean paid;
    private LocalDateTime createdOn;
    private LocalDateTime eventDate;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
    private Long confirmedRequests;
    private Long views;
    private LocationDto location;
}
