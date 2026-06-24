package ru.practicum.common.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.NewEventDto;
import ru.practicum.common.dto.UpdateEventUserRequest;

import java.util.List;

@FeignClient(name = "event-service", path = "/users/{userId}/events", contextId = "privateEventClient")
@Validated
public interface PrivateEventClient {
    
    @GetMapping
    ResponseEntity<List<EventShortDto>> getEvents(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
    
    @PostMapping
    ResponseEntity<EventDto> addEvent(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid NewEventDto dto
    );
    
    @GetMapping("/{eventId}")
    ResponseEntity<EventDto> getEvent(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );
    
    @PatchMapping("/{eventId}")
    ResponseEntity<EventDto> updateEvent(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody @Valid UpdateEventUserRequest updateRequest
    );
}
