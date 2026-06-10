package co.empresa.vivaeventos.events.security;

import co.empresa.vivaeventos.events.domain.model.dto.EventResponse;
import co.empresa.vivaeventos.events.domain.service.IEventService;
import co.empresa.vivaeventos.events.domain.service.ITicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EventControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private IEventService eventService;

    @MockitoBean
    private ITicketService ticketService;

    @Test
    void caso1_sinToken_retorna401() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Evento de prueba",
                "category", "musica",
                "eventDateTime", OffsetDateTime.now().plusDays(30).toString()
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void caso2_tokenInvalido_retorna401() throws Exception {
        when(jwtService.isTokenValid("token-invalido")).thenReturn(false);

        Map<String, Object> body = Map.of(
                "name", "Evento de prueba",
                "category", "musica",
                "eventDateTime", OffsetDateTime.now().plusDays(30).toString()
        );

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer token-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    void caso3_tokenValidoYrolPermitido_retorna200() throws Exception {
        String token = "token-valido-organizador";

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("organizador@test.com");
        when(jwtService.extractRole(token)).thenReturn("ORGANIZER");

        EventResponse mockResponse = new EventResponse();
        mockResponse.setId(UUID.randomUUID());
        mockResponse.setName("Evento de prueba");

        when(eventService.createEvent(any(), any(), any())).thenReturn(mockResponse);

        Map<String, Object> body = Map.of(
                "name", "Evento de prueba",
                "category", "musica",
                "eventDateTime", OffsetDateTime.now().plusDays(30).toString()
        );

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Evento creado exitosamente"));
    }

    @Test
    void caso4_tokenValidoSinRolPermitido_retorna403() throws Exception {
        String token = "token-cliente";

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("cliente@test.com");
        when(jwtService.extractRole(token)).thenReturn("CLIENTE");

        Map<String, Object> body = Map.of(
                "name", "Evento de prueba",
                "category", "musica",
                "eventDateTime", OffsetDateTime.now().plusDays(30).toString()
        );

        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Prohibido"));
    }

    @Test
    void endpointPublicoGetSinToken_retorna200() throws Exception {
        when(eventService.getPublishedEvents()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }
}
