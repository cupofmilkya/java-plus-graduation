package ru.practicum.common.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.dto.UpdateEventAdminRequest;

import java.util.List;

/**
 * Feign client for Admin Events API
 * Matches AdminEventController from main-service
 */
@FeignClient(name = "event-service", path = "/admin/events", contextId = "adminEventClient")
public interface AdminEventClient {
    
    @GetMapping
    ResponseEntity<List<EventDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
    
    @PatchMapping("/{eventId}")
    ResponseEntity<EventDto> updateEvent(
            @PathVariable("eventId") Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateRequest
    );
}
