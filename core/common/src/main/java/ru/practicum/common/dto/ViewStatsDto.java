package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for view statistics - shared across all microservices
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
    
    private String app;
    private String uri;
    private Long hits;
}
