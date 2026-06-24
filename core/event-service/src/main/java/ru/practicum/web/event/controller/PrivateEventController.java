package ru.practicum.web.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.NewEventDto;
import ru.practicum.common.dto.UpdateEventUserRequest;
import ru.practicum.common.client.PrivateEventClient;
import ru.practicum.web.event.service.PrivateEventService;
import ru.practicum.common.exception.BadRequestException;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController implements PrivateEventClient {

    private final PrivateEventService privateEventService;

    @Override
    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        List<EventShortDto> events = privateEventService.getEvents(userId, from, size);
        return ResponseEntity.ok(events);
    }

    @Override
    @PostMapping
    public ResponseEntity<EventDto> addEvent(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid NewEventDto dto
    ) {
        EventDto created = privateEventService.addEvent(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        EventDto event = privateEventService.getEvent(userId, eventId);
        return ResponseEntity.ok(event);
    }

    @Override
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody @Valid UpdateEventUserRequest updateRequest
    ) {
        EventDto updated = privateEventService.updateEvent(userId, eventId, updateRequest);
        return ResponseEntity.ok(updated);
    }
}