package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.dto.EventResponse;
import co.empresa.vivaeventos.events.domain.model.dto.UpdateEventRequest;
import co.empresa.vivaeventos.events.domain.model.EventHistory;

import java.util.List;
import java.util.UUID;

public interface IEventService {

    EventResponse createEvent(UUID organizerId, String userEmail, CreateEventRequest request);

    EventResponse getEventById(UUID eventId);

    List<EventResponse> getEventsByOrganizer(UUID organizerId);

    List<EventResponse> getPublishedEvents();

    List<EventResponse> getPublishedEventsByCategory(String category);

    List<EventResponse> getUpcomingEvents();

    List<EventResponse> getUpcomingEventsByCategory(String category);

    EventResponse updateEvent(UUID eventId, UUID organizerId, String userEmail, UpdateEventRequest request);

    void publishEvent(UUID eventId, UUID organizerId, String userEmail);

    void unpublishEvent(UUID eventId, UUID organizerId, String userEmail);

    void deleteEvent(UUID eventId, UUID organizerId, String userEmail, String motivo);

    void deactivateEvent(UUID eventId, UUID organizerId, String userEmail);

    EventServiceImpl.EventSummary getEventSummary(UUID eventId);

    List<EventHistory> getEventHistory(UUID eventId);
}