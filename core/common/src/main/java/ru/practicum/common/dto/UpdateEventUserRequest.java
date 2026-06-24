package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating event by user - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest {
    
    private String title;
    private String annotation;
    private String description;
    private Long category;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Boolean requestModeration;
    private Integer participantLimit;
    private String stateAction; // CANCEL_REVIEW or SEND_TO_REVIEW
}
