package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "", contextId = "privateRequestClient")
public interface PrivateRequestClient {
    
    @GetMapping("/users/{userId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable("userId") Long userId);
    
    @PostMapping("/users/{userId}/requests")
    ResponseEntity<ParticipationRequestDto> addRequest(
            @PathVariable("userId") Long userId,
            @RequestParam Long eventId
    );
    
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ResponseEntity<ParticipationRequestDto> cancelRequest(
            @PathVariable("userId") Long userId,
            @PathVariable("requestId") Long requestId
    );
    
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getEventRequests(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId
    );
    
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResult> updateRequestsStatus(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest
    );
}
