package co.empresa.vivaeventos.events.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class TicketsClient {

    private final RestTemplate restTemplate;
    private final String ticketsBaseUrl;
    private final SecretKey signingKey;

    public TicketsClient(
            @Value("${services.tickets.url:http://localhost:8085}") String ticketsBaseUrl,
            @Value("${jwt.secret}") String jwtSecret) {
        this.restTemplate = new RestTemplate();
        this.ticketsBaseUrl = ticketsBaseUrl;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private String generateServiceToken() {
        return Jwts.builder()
                .subject("events-service")
                .claim("role", "SYSTEM")
                .claim("userId", "00000000-0000-0000-0000-000000000000")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + generateServiceToken());
        return headers;
    }

    public List<Map<String, Object>> getIssuedTicketsByEvent(UUID eventId) {
        try {
            String url = ticketsBaseUrl + "/api/v1/issued-tickets/event/" + eventId;
            HttpEntity<Void> request = new HttpEntity<>(authHeaders());

            var response = restTemplate.exchange(
                url, HttpMethod.GET, request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("boletas")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> boletas = (List<Map<String, Object>>) body.get("boletas");
                return boletas;
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to get tickets for event {}: {}", eventId, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
