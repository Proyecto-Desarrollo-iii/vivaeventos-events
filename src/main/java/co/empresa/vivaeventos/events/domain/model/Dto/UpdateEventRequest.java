package co.empresa.vivaeventos.events.domain.model.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateEventRequest {

    @Size(min = 1, max = 200, message = "El nombre debe tener entre 1 y 200 caracteres")
    private String name;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String description;

    @Size(min = 1, max = 100, message = "La categoría debe tener entre 1 y 100 caracteres")
    private String category;

    @Future(message = "La fecha del evento debe ser futura")
    private LocalDateTime eventDateTime;

    @Size(max = 500, message = "La URL del banner no puede exceder 500 caracteres")
    private String bannerUrl;

    @Size(max = 500, message = "La URL del thumbnail no puede exceder 500 caracteres")
    private String thumbnailUrl;

    @Size(max = 300, message = "El nombre del venue no puede exceder 300 caracteres")
    private String venueName;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String address;

    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    private Double longitude;

    @Size(max = 1000, message = "La URL del mapa embebido no puede exceder 1000 caracteres")
    private String mapsEmbedUrl;

    @Size(max = 500, message = "La URL del enlace de mapas no puede exceder 500 caracteres")
    private String mapsLinkUrl;

    @Valid
    private List<CreateEventRequest.TicketRequest> tickets;

    @Size(max = 200, message = "El nombre del artista no puede exceder 200 caracteres")
    private String artistName;

    @Size(max = 500, message = "La URL de Spotify no puede exceder 500 caracteres")
    private String spotifyUrl;

    @Size(max = 500, message = "La URL de Instagram no puede exceder 500 caracteres")
    private String instagramUrl;

    @Size(max = 500, message = "La URL de Twitter/X no puede exceder 500 caracteres")
    private String twitterUrl;

    private String city;
    private String location;
    private UUID organizerId;
    private Boolean isPublished;
}