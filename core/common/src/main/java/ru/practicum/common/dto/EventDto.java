package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full event DTO with all details including organizer information.
 * Used for detailed event views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;
    private CategoryDto category;
    private Boolean paid;
    private String eventDate;
    private LocationDto location;
    private Integer participantLimit;
    private String state;
    private String createdOn;
    private String publishedOn;
    private Boolean requestModeration;
    private UserShortDto initiator;
    private Long views;
    private Long confirmedRequests;
}
