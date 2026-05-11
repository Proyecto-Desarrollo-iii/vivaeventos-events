package co.empresa.vivaeventos.events.delivery.rest;

import co.empresa.vivaeventos.events.domain.model.Dto.CreateEventRequest;
import co.empresa.vivaeventos.events.domain.service.ITicketService;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.ConditionResponse;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.QuotaInfo;
import co.empresa.vivaeventos.events.domain.service.TicketServiceImpl.TicketResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TicketController.class,
        excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITicketService ticketService;

    @Test
    void shouldGetTicketsByEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        TicketResponse ticket = new TicketResponse(
                UUID.randomUUID(), eventId, "GENERAL", "desc",
                new BigDecimal("100.00"), 100, 0, true, Collections.emptyList()
        );

        when(ticketService.getTicketsByEvent(eventId)).thenReturn(Collections.singletonList(ticket));

        mockMvc.perform(get("/api/v1/tickets/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.boletas[0].type").value("GENERAL"));
    }

    @Test
    void shouldGetTicketById() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketResponse ticket = new TicketResponse(
                ticketId, UUID.randomUUID(), "VIP", "desc",
                new BigDecimal("250.00"), 50, 0, true, Collections.emptyList()
        );

        when(ticketService.getTicketById(ticketId)).thenReturn(ticket);

        mockMvc.perform(get("/api/v1/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boleta.type").value("VIP"));
    }

    @Test
    void shouldCreateTicket() throws Exception {
        UUID eventId = UUID.randomUUID();
        TicketResponse ticket = new TicketResponse(
                UUID.randomUUID(), eventId, "GENERAL", "desc",
                new BigDecimal("100.00"), 100, 0, true, Collections.emptyList()
        );

        when(ticketService.createTicket(eq(eventId), any(CreateEventRequest.TicketRequest.class))).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/tickets/event/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"GENERAL\",\"price\":100.00,\"capacity\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Tipo de boleta creado exitosamente"));
    }

    @Test
    void shouldUpdateTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        TicketResponse ticket = new TicketResponse(
                ticketId, UUID.randomUUID(), "GENERAL", "desc",
                new BigDecimal("120.00"), 150, 0, true, Collections.emptyList()
        );

        when(ticketService.updateTicket(eq(ticketId), any(CreateEventRequest.TicketRequest.class))).thenReturn(ticket);

        mockMvc.perform(put("/api/v1/tickets/" + ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"GENERAL\",\"price\":120.00,\"capacity\":150}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de boleta actualizado exitosamente"));
    }

    @Test
    void shouldDeleteTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tipo de boleta eliminado exitosamente"));
    }

    @Test
    void shouldReturnNotFoundWhenTicketMissing() throws Exception {
        UUID ticketId = UUID.randomUUID();

        when(ticketService.getTicketById(ticketId)).thenThrow(new RuntimeException("Tipo de boleta no encontrado"));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Tipo de boleta no encontrado"));
    }

    @Test
    void shouldGetQuotaInfo() throws Exception {
        UUID ticketId = UUID.randomUUID();
        QuotaInfo quota = new QuotaInfo(ticketId, 100, 30, 70, false);

        when(ticketService.getQuotaInfo(ticketId)).thenReturn(quota);

        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/cupos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cupo.totalCapacity").value(100))
                .andExpect(jsonPath("$.cupo.availableQuota").value(70));
    }

    @Test
    void shouldGetConditions() throws Exception {
        UUID ticketId = UUID.randomUUID();
        ConditionResponse condition = new ConditionResponse(
                UUID.randomUUID(), ticketId, "EDAD_MINIMA", "18", true
        );

        when(ticketService.getConditionsByTicket(ticketId)).thenReturn(Collections.singletonList(condition));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/condiciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.condiciones[0].type").value("EDAD_MINIMA"));
    }
}
