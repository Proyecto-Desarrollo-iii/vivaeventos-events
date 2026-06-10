package co.empresa.vivaeventos.events.delivery.rest;

import co.empresa.vivaeventos.events.domain.model.dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.dto.UpdateEventRequest;
import co.empresa.vivaeventos.events.domain.service.ITicketService;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.ConditionResponse;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.QuotaInfo;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.TicketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final ITicketService ticketService;

    public TicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Object>> getTicketsByEvent(@PathVariable UUID eventId) {
        try {
            List<TicketResponse> tickets = ticketService.getTicketsByEvent(eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("boletas", tickets);
            response.put("total", tickets.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable UUID ticketId) {
        try {
            TicketResponse ticket = ticketService.getTicketById(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("boleta", ticket);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Object>> createTicket(
            @PathVariable UUID eventId,
            @RequestBody CreateEventRequest.TicketRequest request) {
        try {
            TicketResponse ticket = ticketService.createTicket(eventId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Tipo de boleta creado exitosamente");
            response.put("boleta", ticket);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> updateTicket(
            @PathVariable UUID ticketId,
            @RequestBody CreateEventRequest.TicketRequest request) {
        try {
            TicketResponse ticket = ticketService.updateTicket(ticketId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Tipo de boleta actualizado exitosamente");
            response.put("boleta", ticket);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable UUID ticketId) {
        try {
            ticketService.deleteTicket(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Tipo de boleta eliminado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{ticketId}/condiciones")
    public ResponseEntity<Map<String, Object>> getConditions(@PathVariable UUID ticketId) {
        try {
            List<ConditionResponse> conditions = ticketService.getConditionsByTicket(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("condiciones", conditions);
            response.put("total", conditions.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{ticketId}/condiciones")
    public ResponseEntity<Map<String, Object>> addCondition(
            @PathVariable UUID ticketId,
            @RequestBody CreateEventRequest.TicketRequest.ConditionRequest request) {
        try {
            ticketService.addCondition(ticketId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Condicion agregada exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/condiciones/{conditionId}")
    public ResponseEntity<Map<String, Object>> removeCondition(@PathVariable UUID conditionId) {
        try {
            ticketService.removeCondition(conditionId);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Condicion eliminada exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{ticketId}/cupos")
    public ResponseEntity<Map<String, Object>> getQuotaInfo(@PathVariable UUID ticketId) {
        try {
            QuotaInfo quotaInfo = ticketService.getQuotaInfo(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("cupo", quotaInfo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}