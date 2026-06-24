package ru.practicum.web.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.UpdateEventAdminRequest;
import ru.practicum.web.admin.service.AdminEventService;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.client.AdminEventClient;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Validated
public class AdminEventController implements AdminEventClient {

    private final AdminEventService service;

    @Override
    @GetMapping("/admin/events")
    public ResponseEntity<List<EventDto>> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<EventDto> events = service.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(events);
    }

    @Override
    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventDto> updateEvent(
            @PathVariable("eventId") Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest updateRequest
    ) {
        EventDto updated = service.updateEvent(eventId, updateRequest);
        return ResponseEntity.ok(updated);
    }
}