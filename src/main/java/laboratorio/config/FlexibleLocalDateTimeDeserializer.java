package laboratorio.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();

        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDateTime result = LocalDateTime.parse(dateStr, formatter);
                log.debug("✅ Fecha parseada exitosamente: {} → {}", dateStr, result);
                return result;
            } catch (DateTimeParseException ignored) {
                // Intentar con el siguiente formatter
            }
        }

        log.error("❌ No se pudo parsear la fecha: {}", dateStr);
        throw new IOException("No se pudo parsear la fecha: " + dateStr +
                ". Formatos aceptados: 'yyyy-MM-dd HH:mm:ss.SSS' o 'yyyy-MM-dd'T'HH:mm:ss.SSS'");
    }
}
