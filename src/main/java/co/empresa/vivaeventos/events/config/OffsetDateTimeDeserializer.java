package co.empresa.vivaeventos.events.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Custom deserializer for OffsetDateTime that handles strings without timezone by assuming UTC.
 */
public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String date = p.getText();
        try {
            // Try to parse as OffsetDateTime (with timezone)
            return OffsetDateTime.parse(date);
        } catch (DateTimeParseException e) {
            // If that fails, try to parse as LocalDateTime and assume UTC
            try {
                LocalDateTime ldt = LocalDateTime.parse(date);
                return ldt.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Failed to parse date: " + date, ex);
            }
        }
    }
}