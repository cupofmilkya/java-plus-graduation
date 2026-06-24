package ru.practicum.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating request statuses - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    
    @NotEmpty(message = "Request IDs must not be empty")
    private List<Long> requestIds;
    
    private String status; // CONFIRMED or REJECTED
}
