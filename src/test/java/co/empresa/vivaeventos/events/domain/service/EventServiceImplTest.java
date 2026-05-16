package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.Event;
import co.empresa.vivaeventos.events.domain.model.Ticket;
import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Dto.EventResponse;
import co.empresa.vivaeventos.events.domain.repository.IEventHistoryRepository;
import co.empresa.vivaeventos.events.domain.repository.IEventRepository;
import co.empresa.vivaeventos.events.domain.repository.ITicketConditionRepository;
import co.empresa.vivaeventos.events.domain.repository.ITicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventServiceImplTest {

    @Mock
    private IEventRepository eventRepository;

    @Mock
    private ITicketRepository ticketRepository;

    @Mock
    private ITicketConditionRepository conditionRepository;

    @Mock
    private IEventHistoryRepository historyRepository;

    @Mock
    private TicketValidator ticketValidator;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, ticketRepository, conditionRepository, historyRepository, ticketValidator);
    }

    @Test
    void shouldCreateEvent() {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Test Event");
        request.setDescription("Test Description");
        request.setCategory("musica");
        request.setEventDateTime(LocalDateTime.now().plusDays(7));
        request.setIsPublished(false);

        Event savedEvent = new Event();
        savedEvent.setId(UUID.randomUUID());
        savedEvent.setOrganizerId(organizerId);
        savedEvent.setName(request.getName());
        savedEvent.setDescription(request.getDescription());
        savedEvent.setCategory(request.getCategory());
        savedEvent.setEventDateTime(request.getEventDateTime());
        savedEvent.setIsPublished(false);
        savedEvent.setIsActive(true);

        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(ticketRepository.findByEventId(savedEvent.getId())).thenReturn(java.util.Collections.emptyList());
        when(conditionRepository.findByTicketId(any())).thenReturn(java.util.Collections.emptyList());
        when(ticketValidator.validateTicketsForCreate(any(), any())).thenReturn(java.util.Collections.emptyList());

        EventResponse response = eventService.createEvent(organizerId, "test@example.com", request);

        assertNotNull(response);
        assertEquals("Test Event", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertFalse(response.getIsPublished());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldGetEventById() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();
        event.setId(eventId);
        event.setName("Test Event");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findByEventId(eventId)).thenReturn(java.util.Collections.emptyList());
        when(conditionRepository.findByTicketId(any())).thenReturn(java.util.Collections.emptyList());

        EventResponse response = eventService.getEventById(eventId);

        assertNotNull(response);
        assertEquals("Test Event", response.getName());
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.getEventById(eventId));
    }

    @Test
    void shouldPublishEvent() {
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);
        event.setIsPublished(false);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setEventId(eventId);
        ticket.setPrice(new BigDecimal("100.00"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(ticketRepository.findByEventId(eventId)).thenReturn(java.util.List.of(ticket));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setIsPublished(true);
            return e;
        });
        when(ticketValidator.validateEventForPublishing(eventId, organizerId)).thenReturn(java.util.Collections.emptyList());

        eventService.publishEvent(eventId, organizerId, "test@example.com");

        assertTrue(event.getIsPublished());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void shouldThrowWhenOrganizerDoesNotMatch() {
        UUID eventId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID differentOrganizerId = UUID.randomUUID();

        Event event = new Event();
        event.setId(eventId);
        event.setOrganizerId(organizerId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(RuntimeException.class, () -> eventService.publishEvent(eventId, differentOrganizerId, "test@example.com"));
    }
}