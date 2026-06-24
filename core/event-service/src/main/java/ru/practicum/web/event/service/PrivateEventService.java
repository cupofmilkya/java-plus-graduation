package ru.practicum.web.event.service;

import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.EventDto;
import ru.practicum.common.dto.NewEventDto;
import ru.practicum.common.dto.UpdateEventUserRequest;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventDto addEvent(Long userId, NewEventDto dto);

    EventDto getEvent(Long userId, Long eventId);

    EventDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest);
}