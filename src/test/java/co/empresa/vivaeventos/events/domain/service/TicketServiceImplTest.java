package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Ticket;
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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceImplTest {

    @Mock
    private ITicketRepository ticketRepository;

    @Mock
    private ITicketConditionRepository conditionRepository;

    @Mock
    private TicketValidator ticketValidator;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketRepository, conditionRepository, ticketValidator);
    }

    @Test
    void shouldGetTicketsByEvent() {
        UUID eventId = UUID.randomUUID();
        Ticket ticket = buildTicket(UUID.randomUUID(), eventId, "GENERAL", new BigDecimal("100.00"), 100, 0);

        when(ticketRepository.findByEventId(eventId)).thenReturn(java.util.List.of(ticket));
        when(conditionRepository.findByTicketId(any())).thenReturn(Collections.emptyList());

        var result = ticketService.getTicketsByEvent(eventId);

        assertEquals(1, result.size());
        assertEquals("GENERAL", result.get(0).type());
    }

    @Test
    void shouldGetTicketById() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(ticketId, UUID.randomUUID(), "VIP", new BigDecimal("250.00"), 50, 0);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(conditionRepository.findByTicketId(ticketId)).thenReturn(Collections.emptyList());

        var response = ticketService.getTicketById(ticketId);

        assertNotNull(response);
        assertEquals("VIP", response.type());
    }

    @Test
    void shouldThrowWhenTicketNotFound() {
        UUID ticketId = UUID.randomUUID();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ticketService.getTicketById(ticketId));
    }

    @Test
    void shouldCreateTicket() {
        UUID eventId = UUID.randomUUID();
        CreateEventRequest.TicketRequest request = new CreateEventRequest.TicketRequest();
        request.setType("GENERAL");
        request.setPrice(new BigDecimal("100.00"));
        request.setCapacity(100);

        Ticket saved = buildTicket(UUID.randomUUID(), eventId, "GENERAL", new BigDecimal("100.00"), 100, 0);

        when(ticketValidator.validateTicketRequest(any(), anyInt(), any())).thenReturn(Collections.emptyList());
        when(ticketRepository.save(any(Ticket.class))).thenReturn(saved);
        when(conditionRepository.findByTicketId(any())).thenReturn(Collections.emptyList());

        var response = ticketService.createTicket(eventId, request);

        assertNotNull(response);
        assertEquals("GENERAL", response.type());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldThrowWhenCreateValidationFails() {
        UUID eventId = UUID.randomUUID();
        CreateEventRequest.TicketRequest request = new CreateEventRequest.TicketRequest();

        when(ticketValidator.validateTicketRequest(any(), anyInt(), any()))
                .thenReturn(java.util.List.of("El precio es requerido"));

        assertThrows(RuntimeException.class, () -> ticketService.createTicket(eventId, request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldDeactivateTicketWhenSold() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(ticketId, UUID.randomUUID(), "GENERAL", new BigDecimal("100.00"), 100, 5);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId);

        assertFalse(ticket.getIsActive());
        verify(ticketRepository).save(ticket);
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteTicketWhenNotSold() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(ticketId, UUID.randomUUID(), "GENERAL", new BigDecimal("100.00"), 100, 0);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId);

        verify(conditionRepository).deleteByTicketId(ticketId);
        verify(ticketRepository).delete(ticket);
    }

    @Test
    void shouldGetQuotaInfo() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = buildTicket(ticketId, UUID.randomUUID(), "GENERAL", new BigDecimal("100.00"), 100, 30);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        var info = ticketService.getQuotaInfo(ticketId);

        assertEquals(100, info.totalCapacity());
        assertEquals(30, info.soldCount());
        assertEquals(70, info.availableQuota());
        assertFalse(info.isSoldOut());
    }

    private Ticket buildTicket(UUID id, UUID eventId, String type, BigDecimal price, int capacity, int sold) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setEventId(eventId);
        ticket.setType(type);
        ticket.setPrice(price);
        ticket.setCapacity(capacity);
        ticket.setSoldCount(sold);
        ticket.setIsActive(true);
        return ticket;
    }
}
