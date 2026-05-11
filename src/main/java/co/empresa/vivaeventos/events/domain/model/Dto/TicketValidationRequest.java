package co.empresa.vivaeventos.events.domain.model.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TicketValidationRequest {

    @NotNull(message = "El tipo de boleta es requerido")
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