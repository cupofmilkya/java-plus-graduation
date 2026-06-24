package ru.practicum.web.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.ParticipationRequestDto;
import ru.practicum.common.client.PrivateRequestClient;
import ru.practicum.web.request.service.PrivateRequestService;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class PrivateRequestController implements PrivateRequestClient {

    private final PrivateRequestService requestService;

    @Override
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @Override
    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> addRequest(
            @PathVariable("userId") Long userId,
            @RequestParam Long eventId
    ) {
        ParticipationRequestDto created = requestService.addRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId
    ) {
        return ResponseEntity.ok(requestService.cancelRequest(userId, requestId));
    }

    @Override
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventRequests(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    ) {
        return ResponseEntity.ok(requestService.getEventRequests(userId, eventId));
    }

    @Override
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequestsStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest
    ) {
        return ResponseEntity.ok(requestService.updateRequestsStatus(userId, eventId, statusUpdateRequest));
    }
}