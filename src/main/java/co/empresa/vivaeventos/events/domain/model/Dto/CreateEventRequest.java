package co.empresa.vivaeventos.events.domain.model.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateEventRequest {

    @NotBlank(message = "El nombre del evento es requerido")
    @Size(min = 1, max = 200, message = "El nombre debe tener entre 1 y 200 caracteres")
    private String name;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String description;

    @NotBlank(message = "La categoría es requerida")
    @Size(min = 1, max = 100, message = "La categoría debe tener entre 1 y 100 caracteres")
    private String category;

    @NotNull(message = "La fecha y hora del evento es requerida")
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
    private List<TicketRequest> tickets;

    @Size(max = 200, message = "El nombre del artista no puede exceder 200 caracteres")
    private String artistName;

    @Size(max = 500, message = "La URL de Spotify no puede exceder 500 caracteres")
    private String spotifyUrl;

    @Size(max = 500, message = "La URL de Instagram no puede exceder 500 caracteres")
    private String instagramUrl;

    @Size(max = 500, message = "La URL de Twitter/X no puede exceder 500 caracteres")
    private String twitterUrl;

    private UUID organizerId;

    @Size(max = 200, message = "La ciudad no puede exceder 200 caracteres")
    private String city;

    @Size(max = 500, message = "La ubicación no puede exceder 500 caracteres")
    private String location;

    @Data
    public static class TicketRequest {
        @NotBlank(message = "El tipo de boleta es requerido")
        @Size(min = 1, max = 100, message = "El tipo de boleta debe tener entre 1 y 100 caracteres")
        private String type;

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        private String description;

        @NotNull(message = "El precio es requerido")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener maximo 10 digitos y 2 decimales")
        private BigDecimal price;

        @NotNull(message = "La capacidad es requerida")
        @Min(value = 1, message = "La capacidad minima es 1")
        @Max(value = 100000, message = "La capacidad maxima es 100,000")
        private Integer capacity;

        @Valid
        private List<ConditionRequest> conditions;

        @Data
        public static class ConditionRequest {
            @NotBlank(message = "El tipo de condición es requerido")
            @Size(max = 50, message = "El tipo de condición no puede exceder 50 caracteres")
            private String type;

            @NotBlank(message = "El valor de condición es requerido")
            @Size(max = 255, message = "El valor de condición no puede exceder 255 caracteres")
            private String value;

            private Boolean isActive = true;
        }
    }
}