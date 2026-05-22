package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Dto.EventResponse;
import co.empresa.vivaeventos.events.domain.model.Dto.UpdateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Event;
import co.empresa.vivaeventos.events.domain.model.Ticket;
import co.empresa.vivaeventos.events.domain.model.TicketCondition;
import co.empresa.vivaeventos.events.domain.model.EventHistory;
import co.empresa.vivaeventos.events.domain.repository.IEventHistoryRepository;
import co.empresa.vivaeventos.events.domain.repository.IEventRepository;
import co.empresa.vivaeventos.events.domain.repository.ITicketConditionRepository;
import co.empresa.vivaeventos.events.domain.repository.ITicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements IEventService {

    private final IEventRepository eventRepository;
    private final ITicketRepository ticketRepository;
    private final ITicketConditionRepository conditionRepository;
    private final IEventHistoryRepository historyRepository;
    private final TicketValidator ticketValidator;

    public EventServiceImpl(IEventRepository eventRepository,
                            ITicketRepository ticketRepository,
                            ITicketConditionRepository conditionRepository,
                            IEventHistoryRepository historyRepository,
                            TicketValidator ticketValidator) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.conditionRepository = conditionRepository;
        this.historyRepository = historyRepository;
        this.ticketValidator = ticketValidator;
    }

    @Override
    @Transactional
    public EventResponse createEvent(UUID organizerId, String userEmail, CreateEventRequest request) {
        List<String> validationErrors = ticketValidator.validateTicketsForCreate(
                request.getTickets(),
                request.getEventDateTime()
        );

        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Errores de validacion: " + String.join("; ", validationErrors));
        }

        Event event = new Event();
        event.setOrganizerId(organizerId);
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setEventDateTime(request.getEventDateTime());
        event.setBannerUrl(request.getBannerUrl());
        event.setThumbnailUrl(request.getThumbnailUrl());
        event.setVenueName(request.getVenueName());
        event.setAddress(request.getAddress());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setMapsEmbedUrl(request.getMapsEmbedUrl());
        event.setMapsLinkUrl(request.getMapsLinkUrl());
        event.setCity(request.getCity());
        event.setArtistName(request.getArtistName());
        event.setSpotifyUrl(request.getSpotifyUrl());
        event.setInstagramUrl(request.getInstagramUrl());
        event.setTwitterUrl(request.getTwitterUrl());
        event.setSocialLinks(request.getSocialLinks());
        event.setIsPublished(request.getIsPublished() != null ? request.getIsPublished() : true);
        event.setIsActive(true);
        event.setStatus(request.getIsPublished() == false ? "DRAFT" : "PUBLISHED");

        Event savedEvent = eventRepository.save(event);

        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            for (CreateEventRequest.TicketRequest ticketReq : request.getTickets()) {
                Ticket ticket = new Ticket();
                ticket.setEventId(savedEvent.getId());
                ticket.setType(ticketReq.getType());
                ticket.setDescription(ticketReq.getDescription());
                ticket.setPrice(ticketReq.getPrice());
                ticket.setCapacity(ticketReq.getCapacity());
                ticket.setSoldCount(0);
                ticket.setIsActive(true);

                Ticket savedTicket = ticketRepository.save(ticket);

                if (ticketReq.getConditions() != null && !ticketReq.getConditions().isEmpty()) {
                    for (CreateEventRequest.TicketRequest.ConditionRequest condReq : ticketReq.getConditions()) {
                        TicketCondition condition = new TicketCondition();
                        condition.setTicketId(savedTicket.getId());
                        condition.setType(condReq.getType().toUpperCase());
                        condition.setValue(condReq.getValue());
                        condition.setIsActive(condReq.getIsActive() != null ? condReq.getIsActive() : true);
                        conditionRepository.save(condition);
                    }
                }
            }
        }

        logEventChange(savedEvent.getId(), userEmail, "CREATED",
                "Evento creado: " + savedEvent.getName(), null, eventToStateString(savedEvent));

        return mapEventToResponse(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));
        return mapEventToResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByOrganizer(UUID organizerId) {
        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPublishedEvents() {
        return eventRepository.findByIsPublishedTrueAndIsActiveTrueOrderByEventDateTimeDesc()
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getPublishedEventsByCategory(String category) {
        return eventRepository.findByIsPublishedTrueAndCategoryOrderByEventDateTimeDesc(category)
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now())
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEventsByCategory(String category) {
        return eventRepository.findUpcomingEventsByCategory(category, LocalDateTime.now())
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventResponse updateEvent(UUID eventId, UUID organizerId, String userEmail, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("No tienes permiso para actualizar este evento");
        }

        String prevState = eventToStateString(event);

        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategory() != null) event.setCategory(request.getCategory());
        if (request.getEventDateTime() != null) event.setEventDateTime(request.getEventDateTime());
        if (request.getBannerUrl() != null) event.setBannerUrl(request.getBannerUrl());
        if (request.getThumbnailUrl() != null) event.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getVenueName() != null) event.setVenueName(request.getVenueName());
        if (request.getAddress() != null) event.setAddress(request.getAddress());
        if (request.getLatitude() != null) event.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) event.setLongitude(request.getLongitude());
        if (request.getMapsEmbedUrl() != null) event.setMapsEmbedUrl(request.getMapsEmbedUrl());
        if (request.getMapsLinkUrl() != null) event.setMapsLinkUrl(request.getMapsLinkUrl());
        if (request.getArtistName() != null) event.setArtistName(request.getArtistName());
        if (request.getSpotifyUrl() != null) event.setSpotifyUrl(request.getSpotifyUrl());
        if (request.getInstagramUrl() != null) event.setInstagramUrl(request.getInstagramUrl());
        if (request.getTwitterUrl() != null) event.setTwitterUrl(request.getTwitterUrl());
        if (request.getSocialLinks() != null) event.setSocialLinks(request.getSocialLinks());
        if (request.getCity() != null) event.setCity(request.getCity());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getIsPublished() != null) {
            event.setIsPublished(request.getIsPublished());
            event.setStatus(request.getIsPublished() ? "PUBLISHED" : "DRAFT");
        }

        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            List<String> validationErrors = ticketValidator.validateTicketsForUpdate(
                    request.getTickets(),
                    eventId,
                    event.getEventDateTime()
            );

            if (!validationErrors.isEmpty()) {
                throw new RuntimeException("Errores de validacion: " + String.join("; ", validationErrors));
            }

            List<Ticket> existingTickets = ticketRepository.findByEventId(eventId);
            for (Ticket existingTicket : existingTickets) {
                conditionRepository.deleteByTicketId(existingTicket.getId());
            }
            ticketRepository.deleteAll(existingTickets);

            for (CreateEventRequest.TicketRequest ticketReq : request.getTickets()) {
                Ticket ticket = new Ticket();
                ticket.setEventId(eventId);
                ticket.setType(ticketReq.getType());
                ticket.setDescription(ticketReq.getDescription());
                ticket.setPrice(ticketReq.getPrice());
                ticket.setCapacity(ticketReq.getCapacity());
                ticket.setSoldCount(0);
                ticket.setIsActive(true);

                Ticket savedTicket = ticketRepository.save(ticket);

                if (ticketReq.getConditions() != null && !ticketReq.getConditions().isEmpty()) {
                    for (CreateEventRequest.TicketRequest.ConditionRequest condReq : ticketReq.getConditions()) {
                        TicketCondition condition = new TicketCondition();
                        condition.setTicketId(savedTicket.getId());
                        condition.setType(condReq.getType().toUpperCase());
                        condition.setValue(condReq.getValue());
                        condition.setIsActive(condReq.getIsActive() != null ? condReq.getIsActive() : true);
                        conditionRepository.save(condition);
                    }
                }
            }
        }

        Event updatedEvent = eventRepository.save(event);
        logEventChange(eventId, userEmail, "UPDATED",
                "Evento actualizado: " + updatedEvent.getName(), prevState, eventToStateString(updatedEvent));
        return mapEventToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void publishEvent(UUID eventId, UUID organizerId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("No tienes permiso para publicar este evento");
        }

        List<String> publishErrors = ticketValidator.validateEventForPublishing(eventId, organizerId);
        if (!publishErrors.isEmpty()) {
            throw new RuntimeException("No se puede publicar el evento: " + String.join("; ", publishErrors));
        }

        String prevState = eventToStateString(event);
        event.setIsPublished(true);
        event.setStatus("PUBLISHED");
        eventRepository.save(event);
        logEventChange(eventId, userEmail, "PUBLISHED",
                "Evento publicado: " + event.getName(), prevState, eventToStateString(event));
    }

    @Override
    @Transactional
    public void unpublishEvent(UUID eventId, UUID organizerId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("No tienes permiso para despublicar este evento");
        }

        String prevState = eventToStateString(event);
        event.setIsPublished(false);
        event.setStatus("DRAFT");
        eventRepository.save(event);
        logEventChange(eventId, userEmail, "UNPUBLISHED",
                "Evento despublicado: " + event.getName(), prevState, eventToStateString(event));
    }

    @Override
    @Transactional
    public void deleteEvent(UUID eventId, UUID organizerId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("No tienes permiso para eliminar este evento");
        }

        String prevState = eventToStateString(event);
        logEventChange(eventId, userEmail, "DELETED",
                "Evento eliminado: " + event.getName(), prevState, null);

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        for (Ticket ticket : tickets) {
            conditionRepository.deleteByTicketId(ticket.getId());
        }

        ticketRepository.deleteAll(tickets);
        eventRepository.delete(event);
    }

    @Override
    @Transactional
    public void deactivateEvent(UUID eventId, UUID organizerId, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("No tienes permiso para desactivar este evento");
        }

        String prevState = eventToStateString(event);
        event.setIsActive(false);
        event.setStatus("DEACTIVATED");
        eventRepository.save(event);
        logEventChange(eventId, userEmail, "DEACTIVATED",
                "Evento desactivado: " + event.getName(), prevState, eventToStateString(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventSummary getEventSummary(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventId));

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        int totalCapacity = tickets.stream().mapToInt(Ticket::getCapacity).sum();
        int totalSold = tickets.stream().mapToInt(Ticket::getSoldCount).sum();
        int totalAvailable = totalCapacity - totalSold;
        int ticketTypes = tickets.size();

        return new EventSummary(
                eventId,
                event.getName(),
                totalCapacity,
                totalSold,
                totalAvailable,
                ticketTypes,
                tickets.stream().anyMatch(t -> t.getCapacity() - t.getSoldCount() <= 0)
        );
    }

    private EventResponse mapEventToResponse(Event event) {
        List<Ticket> tickets = ticketRepository.findByEventId(event.getId());
        List<EventResponse.TicketResponse> ticketResponses = tickets.stream()
                .map(t -> {
                    List<TicketCondition> conditions = conditionRepository.findByTicketId(t.getId());
                    List<EventResponse.ConditionResponse> conditionResponses = conditions.stream()
                            .map(c -> {
                                EventResponse.ConditionResponse cr = new EventResponse.ConditionResponse();
                                cr.setId(c.getId());
                                cr.setTicketId(c.getTicketId());
                                cr.setType(c.getType());
                                cr.setValue(c.getValue());
                                cr.setIsActive(c.getIsActive());
                                return cr;
                            })
                            .toList();

                    EventResponse.TicketResponse tr = new EventResponse.TicketResponse();
                    tr.setId(t.getId());
                    tr.setEventId(t.getEventId());
                    tr.setType(t.getType());
                    tr.setDescription(t.getDescription());
                    tr.setPrice(t.getPrice());
                    tr.setCapacity(t.getCapacity());
                    tr.setSoldCount(t.getSoldCount());
                    tr.setIsActive(t.getIsActive());
                    tr.setConditions(conditionResponses);
                    return tr;
                })
                .collect(Collectors.toList());

        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setOrganizerId(event.getOrganizerId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setCategory(event.getCategory());
        response.setEventDateTime(event.getEventDateTime());
        response.setBannerUrl(event.getBannerUrl());
        response.setThumbnailUrl(event.getThumbnailUrl());
        response.setVenueName(event.getVenueName());
        response.setAddress(event.getAddress());
        response.setLatitude(event.getLatitude());
        response.setLongitude(event.getLongitude());
        response.setMapsEmbedUrl(event.getMapsEmbedUrl());
        response.setMapsLinkUrl(event.getMapsLinkUrl());
        response.setArtistName(event.getArtistName());
        response.setSpotifyUrl(event.getSpotifyUrl());
        response.setInstagramUrl(event.getInstagramUrl());
        response.setTwitterUrl(event.getTwitterUrl());
        response.setSocialLinks(event.getSocialLinks());
        response.setCity(event.getCity());
        response.setLocation(event.getLocation());
        response.setStatus(event.getStatus());
        response.setIsPublished(event.getIsPublished());
        response.setIsActive(event.getIsActive());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        response.setTickets(ticketResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventHistory> getEventHistory(UUID eventId) {
        return historyRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    private void logEventChange(UUID eventId, String userEmail, String action, String description, String previousState, String newState) {
        EventHistory history = new EventHistory();
        history.setEventId(eventId);
        history.setUserEmail(userEmail);
        history.setAction(action);
        history.setDescription(description);
        history.setPreviousState(previousState);
        history.setNewState(newState);
        historyRepository.save(history);
    }

    private String eventToStateString(Event event) {
        return String.format("name=%s, category=%s, date=%s, venue=%s, published=%s, active=%s",
                event.getName(), event.getCategory(), event.getEventDateTime(),
                event.getVenueName(), event.getIsPublished(), event.getIsActive());
    }

    public record EventSummary(
            UUID eventId,
            String eventName,
            Integer totalCapacity,
            Integer totalSold,
            Integer totalAvailable,
            Integer ticketTypes,
            Boolean hasSoldOutTickets
    ) {}
}