package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/events", contextId = "publicEventClient")
public interface PublicEventClient {
    
    @GetMapping
    ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );
    
    @GetMapping("/{id}")
    ResponseEntity<EventDto> getEvent(@PathVariable("id") Long id);
}
