package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Dto.UpdateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Ticket;
import co.empresa.vivaeventos.events.domain.repository.ITicketRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class TicketValidator {

    private final ITicketRepository ticketRepository;
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999.99");
    private static final int MAX_TICKETS_PER_EVENT = 20;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w.-]+)\\.([a-z]{2,})(/.*)?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> VALID_CONDITION_TYPES = Set.of(
            "EDAD_MINIMA",
            "EDAD_MAXIMA",
            "VESTIMENTA",
            "PROHIBICIONES",
            "ACCESO_ESPECIAL",
            "DESCUENTO_GRUPO",
            "ZONA_FUMADORES",
            "PLATINO",
            "VIP_LOUNGE",
            "PARKING",
            "METODO_PAGO",
            "RESTRICCION_HORARIA",
            "PROMOCION"
    );

    public TicketValidator(ITicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<String> validateTicketsForCreate(List<CreateEventRequest.TicketRequest> tickets, OffsetDateTime eventDateTime) {
        List<String> errors = new ArrayList<>();

        if (tickets == null || tickets.isEmpty()) {
            errors.add("Debe incluir al menos un tipo de boleta");
            return errors;
        }

        if (tickets.size() > MAX_TICKETS_PER_EVENT) {
            errors.add("No puede crear mas de " + MAX_TICKETS_PER_EVENT + " tipos de boleta por evento");
        }

        Set<String> ticketTypes = new HashSet<>();
        int index = 0;

        for (CreateEventRequest.TicketRequest ticket : tickets) {
            index++;
            errors.addAll(validateTicketRequest(ticket, index, ticketTypes));
        }

        return errors;
    }

    public List<String> validateTicketsForUpdate(List<CreateEventRequest.TicketRequest> tickets,
                                                    java.util.UUID eventId,
                                                    OffsetDateTime eventDateTime) {
        List<String> errors = new ArrayList<>();

        if (tickets == null || tickets.isEmpty()) {
            errors.add("Debe incluir al menos un tipo de boleta");
            return errors;
        }

        if (tickets.size() > MAX_TICKETS_PER_EVENT) {
            errors.add("No puede crear mas de " + MAX_TICKETS_PER_EVENT + " tipos de boleta por evento");
        }

        Set<String> ticketTypes = new HashSet<>();
        int index = 0;

        for (CreateEventRequest.TicketRequest ticket : tickets) {
            index++;
            errors.addAll(validateTicketRequest(ticket, index, ticketTypes));

            errors.addAll(validateTicketCapacity(ticket, eventId));
        }

        return errors;
    }

    public List<String> validateTicketRequest(CreateEventRequest.TicketRequest ticket, int index, Set<String> existingTypes) {
        List<String> errors = new ArrayList<>();

        String prefix = "Boleta " + index + ": ";

        if (ticket.getType() == null || ticket.getType().trim().isEmpty()) {
            errors.add(prefix + "El tipo de boleta es requerido");
        } else {
            String normalizedType = normalizeTicketType(ticket.getType());

            if (existingTypes.contains(normalizedType)) {
                errors.add(prefix + "No puede haber tipos de boleta duplicados");
            } else {
                existingTypes.add(normalizedType);
            }
        }

        if (ticket.getPrice() == null) {
            errors.add(prefix + "El precio es requerido");
        } else {
            if (ticket.getPrice().compareTo(MIN_PRICE) < 0) {
                errors.add(prefix + "El precio debe ser mayor a " + MIN_PRICE);
            }
            if (ticket.getPrice().compareTo(MAX_PRICE) > 0) {
                errors.add(prefix + "El precio no puede exceder " + MAX_PRICE);
            }
        }

        if (ticket.getCapacity() == null) {
            errors.add(prefix + "La capacidad es requerida");
        } else if (ticket.getCapacity() < 1) {
            errors.add(prefix + "La capacidad minima es 1");
        } else if (ticket.getCapacity() > 100000) {
            errors.add(prefix + "La capacidad maxima es 100,000");
        }

        if (ticket.getConditions() != null) {
            errors.addAll(validateConditions(ticket.getConditions(), prefix));
        }

        return errors;
    }

    private List<String> validateConditions(List<CreateEventRequest.TicketRequest.ConditionRequest> conditions, String prefix) {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < conditions.size(); i++) {
            CreateEventRequest.TicketRequest.ConditionRequest condition = conditions.get(i);
            String condPrefix = prefix + "Condicion " + (i + 1) + ": ";

            if (condition.getType() == null || condition.getType().trim().isEmpty()) {
                errors.add(condPrefix + "El tipo de condicion es requerido");
            } else {
                String normalizedType = condition.getType().trim().toUpperCase();
                if (!VALID_CONDITION_TYPES.contains(normalizedType)) {
                    errors.add(condPrefix + "Tipo de condicion invalido. Tipos validos: " + VALID_CONDITION_TYPES);
                }
            }

            if (condition.getValue() == null || condition.getValue().trim().isEmpty()) {
                errors.add(condPrefix + "El valor de condicion es requerido");
            }
        }

        return errors;
    }

    private List<String> validateTicketCapacity(CreateEventRequest.TicketRequest ticketRequest, java.util.UUID eventId) {
        List<String> errors = new ArrayList<>();

        List<Ticket> existingTickets = ticketRepository.findByEventId(eventId);
        for (Ticket existingTicket : existingTickets) {
            if (existingTicket.getType().equalsIgnoreCase(ticketRequest.getType().trim())) {
                if (ticketRequest.getCapacity() < existingTicket.getSoldCount()) {
                    errors.add("La nueva capacidad (" + ticketRequest.getCapacity() +
                            ") no puede ser menor a la cantidad ya vendida (" +
                            existingTicket.getSoldCount() + ")");
                }
                break;
            }
        }

        return errors;
    }

    public List<String> validateEventForPublishing(java.util.UUID eventId, java.util.UUID organizerId) {
        List<String> errors = new ArrayList<>();

        List<Ticket> tickets = ticketRepository.findByEventId(eventId);

        if (tickets.isEmpty()) {
            errors.add("El evento debe tener al menos un tipo de boleta para ser publicado");
        }

        for (Ticket ticket : tickets) {
            if (ticket.getPrice() == null || ticket.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Todas las boletas deben tener un precio valido");
                break;
            }
        }

        return errors;
    }

    public List<String> validateQuotaUpdate(java.util.UUID ticketId, Integer newCapacity) {
        List<String> errors = new ArrayList<>();

        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) {
            errors.add("Tipo de boleta no encontrado");
            return errors;
        }

        if (newCapacity < ticket.getSoldCount()) {
            errors.add("La nueva capacidad (" + newCapacity +
                    ") no puede ser menor a la cantidad ya vendida (" +
                    ticket.getSoldCount() + ")");
        }

        return errors;
    }

    public String normalizeTicketType(String type) {
        return type.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    public int calculateTotalCapacity(List<CreateEventRequest.TicketRequest> tickets) {
        return tickets.stream()
                .filter(t -> t.getCapacity() != null)
                .mapToInt(CreateEventRequest.TicketRequest::getCapacity)
                .sum();
    }

    public int calculateTotalSold(java.util.UUID eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream()
                .mapToInt(Ticket::getSoldCount)
                .sum();
    }

    public boolean hasAvailableQuota(java.util.UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) return false;

        int available = ticket.getCapacity() - ticket.getSoldCount();
        return available > 0;
    }

    public int getAvailableQuota(java.util.UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null) return 0;

        return Math.max(0, ticket.getCapacity() - ticket.getSoldCount());
    }

    public boolean isTicketSoldOut(java.util.UUID ticketId) {
        return !hasAvailableQuota(ticketId);
    }
}