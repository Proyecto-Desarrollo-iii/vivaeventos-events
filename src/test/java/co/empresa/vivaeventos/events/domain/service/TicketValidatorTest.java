package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Ticket;
import co.empresa.vivaeventos.events.domain.repository.ITicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketValidatorTest {

    @Mock
    private ITicketRepository ticketRepository;

    private TicketValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TicketValidator(ticketRepository);
    }

    @Test
    void shouldFailWhenTicketsEmpty() {
        List<String> errors = validator.validateTicketsForCreate(List.of(), null);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("al menos un tipo de boleta"));
    }

    @Test
    void shouldValidateTicketRequestSuccessfully() {
        CreateEventRequest.TicketRequest ticket = buildTicketRequest("GENERAL", new BigDecimal("100.00"), 100);

        List<String> errors = validator.validateTicketRequest(ticket, 1, new HashSet<>());

        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldFailWhenPriceTooLow() {
        CreateEventRequest.TicketRequest ticket = buildTicketRequest("GENERAL", new BigDecimal("0.00"), 100);

        List<String> errors = validator.validateTicketRequest(ticket, 1, new HashSet<>());

        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("precio")));
    }

    @Test
    void shouldFailWhenCapacityNull() {
        CreateEventRequest.TicketRequest ticket = buildTicketRequest("GENERAL", new BigDecimal("100.00"), null);

        List<String> errors = validator.validateTicketRequest(ticket, 1, new HashSet<>());

        assertTrue(errors.stream().anyMatch(e -> e.contains("capacidad")));
    }

    @Test
    void shouldFailWhenDuplicateTypes() {
        HashSet<String> existing = new HashSet<>();
        existing.add("GENERAL");

        CreateEventRequest.TicketRequest ticket = buildTicketRequest("general", new BigDecimal("100.00"), 100);

        List<String> errors = validator.validateTicketRequest(ticket, 2, existing);

        assertTrue(errors.stream().anyMatch(e -> e.contains("duplicados")));
    }

    @Test
    void shouldFailWhenConditionTypeInvalid() {
        CreateEventRequest.TicketRequest ticket = buildTicketRequest("GENERAL", new BigDecimal("100.00"), 100);
        CreateEventRequest.TicketRequest.ConditionRequest condition = new CreateEventRequest.TicketRequest.ConditionRequest();
        condition.setType("INVALIDO");
        condition.setValue("valor");
        ticket.setConditions(List.of(condition));

        List<String> errors = validator.validateTicketRequest(ticket, 1, new HashSet<>());

        assertTrue(errors.stream().anyMatch(e -> e.contains("Tipo de condicion invalido")));
    }

    @Test
    void shouldNormalizeTicketType() {
        assertEquals("VIP_PREMIUM", validator.normalizeTicketType(" vip premium "));
    }

    @Test
    void shouldFailQuotaUpdateWhenLowerThanSold() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setCapacity(100);
        ticket.setSoldCount(50);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        List<String> errors = validator.validateQuotaUpdate(ticketId, 30);

        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("no puede ser menor"));
    }

    @Test
    void shouldFailPublishingWhenNoTickets() {
        UUID eventId = UUID.randomUUID();

        when(ticketRepository.findByEventId(eventId)).thenReturn(List.of());

        List<String> errors = validator.validateEventForPublishing(eventId, UUID.randomUUID());

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("al menos un tipo de boleta"));
    }

    private CreateEventRequest.TicketRequest buildTicketRequest(String type, BigDecimal price, Integer capacity) {
        CreateEventRequest.TicketRequest request = new CreateEventRequest.TicketRequest();
        request.setType(type);
        request.setPrice(price);
        request.setCapacity(capacity);
        return request;
    }
}
