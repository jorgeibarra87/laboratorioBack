package laboratorio.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_DATE_TIME
    };

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String fechaStr = parser.getText();

        // ✅ Primero verificar si es un timestamp numérico
        try {
            long timestamp = Long.parseLong(fechaStr);
            LocalDateTime result = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            log.debug("✅ Fecha parseada desde timestamp: {} -> {}", timestamp, result);
            return result;
        } catch (NumberFormatException e) {
            // No es un timestamp, continuar con formatos de texto
        }

        // ✅ Intentar con los formatos de texto
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDateTime result = LocalDateTime.parse(fechaStr, formatter);
                log.debug("✅ Fecha parseada: {} -> {}", fechaStr, result);
                return result;
            } catch (DateTimeParseException e) {
                // Continuar con el siguiente formato
            }
        }
        // ❌ Si ninguno funciona, lanzar error
        String mensaje = String.format(
                "No se pudo parsear la fecha: %s. Formatos aceptados: timestamp en milisegundos o 'yyyy-MM-dd HH:mm:ss.SSS' o 'yyyy-MM-dd'T'HH:mm:ss.SSS'",
                fechaStr
        );
        log.error("❌ {}", mensaje);
        throw new IOException(mensaje);
    }
}