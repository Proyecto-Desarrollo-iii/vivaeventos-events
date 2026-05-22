package co.empresa.vivaeventos.events.delivery.rest;

import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.model.Dto.EventResponse;
import co.empresa.vivaeventos.events.domain.model.Dto.UpdateEventRequest;
import co.empresa.vivaeventos.events.domain.model.EventHistory;
import co.empresa.vivaeventos.events.domain.service.EventServiceImpl;
import co.empresa.vivaeventos.events.domain.service.IEventService;
import co.empresa.vivaeventos.events.domain.service.ITicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final IEventService eventService;
    private final ITicketService ticketService;

    public EventController(IEventService eventService, ITicketService ticketService) {
        this.eventService = eventService;
        this.ticketService = ticketService;
    }

  /*  @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(
            Authentication authentication,
            @RequestBody CreateEventRequest request) {
        try {
            String organizerId = authentication.getName();
            UUID orgId = UUID.fromString(extractUserIdFromToken(authentication));

            EventResponse eventResponse = eventService.createEvent(orgId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento creado exitosamente");
            response.put("evento", eventResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }*/
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

        try {
            UUID orgId = request.getOrganizerId();
            if (orgId == null) {
                if (userEmail != null) {
                    orgId = UUID.nameUUIDFromBytes(userEmail.getBytes());
                } else {
                    orgId = UUID.randomUUID();
                }
            }

            EventResponse eventResponse =
                    eventService.createEvent(orgId, userEmail, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento creado exitosamente");
            response.put("evento", eventResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getEvent(@PathVariable UUID eventId) {
        try {
            EventResponse eventResponse = eventService.getEventById(eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("evento", eventResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<Map<String, Object>> getEventsByOrganizer(@PathVariable UUID organizerId) {
        try {
            List<EventResponse> events = eventService.getEventsByOrganizer(organizerId);

            Map<String, Object> response = new HashMap<>();
            response.put("eventos", events);
            response.put("total", events.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPublishedEvents(
            @RequestParam(required = false) String category) {
        try {
            List<EventResponse> events;

            if (category != null && !category.isEmpty()) {
                events = eventService.getPublishedEventsByCategory(category);
            } else {
                events = eventService.getPublishedEvents();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("eventos", events);
            response.put("total", events.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingEvents(
            @RequestParam(required = false) String category) {
        try {
            List<EventResponse> events;

            if (category != null && !category.isEmpty()) {
                events = eventService.getUpcomingEventsByCategory(category);
            } else {
                events = eventService.getUpcomingEvents();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("eventos", events);
            response.put("total", events.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            UUID organizerId = request.getOrganizerId();
            if (organizerId == null) {
                if (userEmail != null) {
                    organizerId = UUID.nameUUIDFromBytes(userEmail.getBytes());
                } else {
                    organizerId = UUID.randomUUID();
                }
            }

            EventResponse eventResponse = eventService.updateEvent(eventId, organizerId, userEmail, request);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento actualizado exitosamente");
            response.put("evento", eventResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{eventId}/publish")
    public ResponseEntity<Map<String, Object>> publishEvent(
            @PathVariable UUID eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            UUID organizerId = extractOrganizerId(body, userEmail);

            eventService.publishEvent(eventId, organizerId, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento publicado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @PostMapping("/{eventId}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishEvent(
            @PathVariable UUID eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            UUID organizerId = extractOrganizerId(body, userEmail);

            eventService.unpublishEvent(eventId, organizerId, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento despublicado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @PostMapping("/{eventId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateEvent(
            @PathVariable UUID eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            UUID organizerId = extractOrganizerId(body, userEmail);

            eventService.deactivateEvent(eventId, organizerId, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento desactivado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @PostMapping("/{eventId}/delete")
    public ResponseEntity<Map<String, Object>> deleteEvent(
            @PathVariable UUID eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            UUID organizerId = extractOrganizerId(body, userEmail);

            eventService.deleteEvent(eventId, organizerId, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Evento eliminado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    @GetMapping("/{eventId}/resumen-cupos")
    public ResponseEntity<Map<String, Object>> getEventSummary(@PathVariable UUID eventId) {
        try {
            EventServiceImpl.EventSummary summary = eventService.getEventSummary(eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("resumen", summary);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/tickets/{ticketId}/vender")
    public ResponseEntity<Map<String, Object>> incrementTicketSales(
            @PathVariable UUID ticketId,
            @RequestBody Map<String, Integer> body) {
        try {
            int quantity = body.getOrDefault("cantidad", 1);
            ticketService.incrementSoldCount(ticketId, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Venta registrada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{eventId}/history")
    public ResponseEntity<Map<String, Object>> getEventHistory(@PathVariable UUID eventId) {
        try {
            List<EventHistory> history = eventService.getEventHistory(eventId);

            Map<String, Object> response = new HashMap<>();
            response.put("history", history);
            response.put("total", history.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    private UUID extractOrganizerId(Map<String, Object> body, String userEmail) {
        if (body != null && body.containsKey("organizerId") && body.get("organizerId") != null) {
            return UUID.fromString(body.get("organizerId").toString());
        }
        return userEmail != null ? UUID.nameUUIDFromBytes(userEmail.getBytes()) : UUID.randomUUID();
    }

}