package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Ticket;
import co.empresa.vivaeventos.events.domain.model.TicketCondition;
import co.empresa.vivaeventos.events.domain.repository.ITicketConditionRepository;
import co.empresa.vivaeventos.events.domain.repository.ITicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final ITicketConditionRepository conditionRepository;
    private final TicketValidator ticketValidator;

    public TicketServiceImpl(ITicketRepository ticketRepository,
                             ITicketConditionRepository conditionRepository,
                             TicketValidator ticketValidator) {
        this.ticketRepository = ticketRepository;
        this.conditionRepository = conditionRepository;
        this.ticketValidator = ticketValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByEvent(UUID eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tipo de boleta no encontrado: " + ticketId));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse createTicket(UUID eventId, CreateEventRequest.TicketRequest request) {
        List<String> errors = ticketValidator.validateTicketRequest(request, 1, new java.util.HashSet<>());
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join("; ", errors));
        }

        Ticket ticket = new Ticket();
        ticket.setEventId(eventId);
        ticket.setType(request.getType());
        ticket.setDescription(request.getDescription());
        ticket.setPrice(request.getPrice());
        ticket.setCapacity(request.getCapacity());
        ticket.setSoldCount(0);
        ticket.setIsActive(true);

        Ticket savedTicket = ticketRepository.save(ticket);

        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            for (CreateEventRequest.TicketRequest.ConditionRequest condReq : request.getConditions()) {
                TicketCondition condition = new TicketCondition();
                condition.setTicketId(savedTicket.getId());
                condition.setType(condReq.getType().toUpperCase());
                condition.setValue(condReq.getValue());
                condition.setIsActive(condReq.getIsActive() != null ? condReq.getIsActive() : true);
                conditionRepository.save(condition);
            }
        }

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(UUID ticketId, CreateEventRequest.TicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tipo de boleta no encontrado: " + ticketId));

        List<String> capacityErrors = ticketValidator.validateQuotaUpdate(ticketId, request.getCapacity());
        if (!capacityErrors.isEmpty()) {
            throw new RuntimeException(String.join("; ", capacityErrors));
        }

        List<String> validationErrors = ticketValidator.validateTicketRequest(request, 1, new java.util.HashSet<>());
        if (!validationErrors.isEmpty()) {
            throw new RuntimeException(String.join("; ", validationErrors));
        }

        ticket.setType(request.getType());
        ticket.setDescription(request.getDescription());
        ticket.setPrice(request.getPrice());
        ticket.setCapacity(request.getCapacity());

        Ticket updatedTicket = ticketRepository.save(ticket);

        if (request.getConditions() != null) {
            conditionRepository.deleteByTicketId(ticketId);

            for (CreateEventRequest.TicketRequest.ConditionRequest condReq : request.getConditions()) {
                TicketCondition condition = new TicketCondition();
                condition.setTicketId(ticketId);
                condition.setType(condReq.getType().toUpperCase());
                condition.setValue(condReq.getValue());
                condition.setIsActive(condReq.getIsActive() != null ? condReq.getIsActive() : true);
                conditionRepository.save(condition);
            }
        }

        return mapToResponse(updatedTicket);
    }

    @Override
    @Transactional
    public void deleteTicket(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tipo de boleta no encontrado: " + ticketId));

        if (ticket.getSoldCount() > 0) {
            ticket.setIsActive(false);
            ticketRepository.save(ticket);
        } else {
            conditionRepository.deleteByTicketId(ticketId);
            ticketRepository.delete(ticket);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getConditionsByTicket(UUID ticketId) {
        List<TicketCondition> conditions = conditionRepository.findByTicketId(ticketId);
        return conditions.stream().map(c -> new ConditionResponse(
                c.getId(),
                c.getTicketId(),
                c.getType(),
                c.getValue(),
                c.getIsActive()
        )).toList();
    }

    @Override
    @Transactional
    public void addCondition(UUID ticketId, CreateEventRequest.TicketRequest.ConditionRequest conditionRequest) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tipo de boleta no encontrado: " + ticketId));

        TicketCondition condition = new TicketCondition();
        condition.setTicketId(ticketId);
        condition.setType(conditionRequest.getType().toUpperCase());
        condition.setValue(conditionRequest.getValue());
        condition.setIsActive(conditionRequest.getIsActive() != null ? conditionRequest.getIsActive() : true);

        conditionRepository.save(condition);
    }

    @Override
    @Transactional
    public void removeCondition(UUID conditionId) {
        TicketCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new RuntimeException("Condicion no encontrada: " + conditionId));
        conditionRepository.delete(condition);
    }

    @Override
    @Transactional(readOnly = true)
    public QuotaInfo getQuotaInfo(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Tipo de boleta no encontrado: " + ticketId));

        int total = ticket.getCapacity();
        int sold = ticket.getSoldCount();
        int available = Math.max(0, total - sold);
        boolean isSoldOut = available == 0;

        return new QuotaInfo(ticketId, total, sold, available, isSoldOut);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        List<TicketCondition> conditions = conditionRepository.findByTicketId(ticket.getId());
        List<ConditionResponse> conditionResponses = conditions.stream()
                .map(c -> new ConditionResponse(
                        c.getId(),
                        c.getTicketId(),
                        c.getType(),
                        c.getValue(),
                        c.getIsActive()
                ))
                .toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getEventId(),
                ticket.getType(),
                ticket.getDescription(),
                ticket.getPrice(),
                ticket.getCapacity(),
                ticket.getSoldCount(),
                ticket.getIsActive(),
                conditionResponses
        );
    }

    public record TicketResponse(
            UUID id,
            UUID eventId,
            String type,
            String description,
            java.math.BigDecimal price,
            Integer capacity,
            Integer soldCount,
            Boolean isActive,
            List<ConditionResponse> conditions
    ) {}

    public record ConditionResponse(
            UUID id,
            UUID ticketId,
            String type,
            String value,
            Boolean isActive
    ) {}

    public record QuotaInfo(
            UUID ticketId,
            Integer totalCapacity,
            Integer soldCount,
            Integer availableQuota,
            Boolean isSoldOut
    ) {}
}