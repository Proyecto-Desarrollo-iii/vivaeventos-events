package co.empresa.vivaeventos.events.domain.service;

import co.empresa.vivaeventos.events.domain.model.dto.CreateEventRequest;

import java.util.List;
import java.util.UUID;

public interface ITicketService {

    List<TicketServiceImpl.TicketResponse> getTicketsByEvent(UUID eventId);

    TicketServiceImpl.TicketResponse getTicketById(UUID ticketId);

    TicketServiceImpl.TicketResponse createTicket(UUID eventId, CreateEventRequest.TicketRequest request);

    TicketServiceImpl.TicketResponse updateTicket(UUID ticketId, CreateEventRequest.TicketRequest request);

    void deleteTicket(UUID ticketId);

    List<TicketServiceImpl.ConditionResponse> getConditionsByTicket(UUID ticketId);

    void addCondition(UUID ticketId, CreateEventRequest.TicketRequest.ConditionRequest conditionRequest);

    void removeCondition(UUID conditionId);

    TicketServiceImpl.QuotaInfo getQuotaInfo(UUID ticketId);
}