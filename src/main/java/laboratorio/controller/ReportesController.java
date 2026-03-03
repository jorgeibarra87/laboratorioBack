package laboratorio.controller;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
public class ReportesController {

    // helper
    private static class ExamenCatalogoEntry {
        String nombreCompleto;
        String abreviatura;
        String tipoMuestra; // sangre, orina, etc.
        String colorTubo;   // lila, rojo, etc.

        ExamenCatalogoEntry(String nombreCompleto, String abreviatura, String tipoMuestra, String colorTubo) {
            this.nombreCompleto = nombreCompleto;
            this.abreviatura = abreviatura;
            this.tipoMuestra = tipoMuestra;
            this.colorTubo = colorTubo;
        }
    }

    // Simulación de “tabla” de catálogo (en memoria)
    private static final Map<String, ExamenCatalogoEntry> CATALOGO_EXAMENES = new HashMap<>();

    static {
        // Clave: descCups (nombre ex.descProcedimiento)
        CATALOGO_EXAMENES.put("RESONANCIA MAGNÉTICA DE COLUMNA",
                new ExamenCatalogoEntry("RESONANCIA MAGNÉTICA DE COLUMNA", "RCOL", "IMAGEN", "SIN TUBO"));
        CATALOGO_EXAMENES.put("RESONANCIA MAGNÉTICA DE CEREBRO",
                new ExamenCatalogoEntry("RESONANCIA MAGNÉTICA DE CEREBRO", "RCER", "IMAGEN", "SIN TUBO"));
        // Ejemplos de laboratorio
        CATALOGO_EXAMENES.put("HEMOGRAMA IV",
                new ExamenCatalogoEntry("HEMOGRAMA IV", "HEMIV", "SANGRE", "LILA"));
        CATALOGO_EXAMENES.put("PROTEINA C REACTIVA ALTA PRECISION AUTOMATIZADA",
                new ExamenCatalogoEntry("PROTEINA C REACTIVA ALTA PRECISION AUTOMATIZADA", "PCRAP", "SANGRE", "LILA"));
    }

    // Obtiene abreviatura; si no existe, genera una simple
    private String obtenerAbreviaturaExamen(String nombreExamen) {
        if (nombreExamen == null || nombreExamen.isBlank()) return "";

        ExamenCatalogoEntry entry = CATALOGO_EXAMENES.get(nombreExamen.trim().toUpperCase());
        if (entry != null) {
            return entry.abreviatura;
        }

        // Fallback: generar abreviatura simple (primeras letras de cada palabra)
        String[] partes = nombreExamen.toUpperCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : partes) {
            if (!p.isBlank() && Character.isLetter(p.charAt(0))) {
                sb.append(p.charAt(0));
            }
            if (sb.length() >= 6) break; // limita longitud
        }
        return sb.length() > 0 ? sb.toString() : nombreExamen.toUpperCase();
    }



    @PostMapping("/stickers-pdf")
    public ResponseEntity<byte[]> generarStickersPDF(
            @RequestBody List<Map<String, Object>> examenes) {

        try {
            log.info("📄 Generando PDF de stickers para {} exámenes (entrada)", examenes.size());

            // AGRUPAR POR PACIENTE+FOLIO
            Map<String, List<Map<String, Object>>> agrupadoPorPaciente = new LinkedHashMap<>();

            for (Map<String, Object> ex : examenes) {
                String documento = getStringValue(ex, "documento");
                String folio = getStringValue(ex, "numeroFolio", getStringValue(ex, "folio", ""));
                String key = documento + "::" + folio; // agrupar por paciente+folio

                agrupadoPorPaciente.computeIfAbsent(key, k -> new ArrayList<>()).add(ex);
            }

            log.info("👥 Se generarán {} sticker(s) (paciente+folio)", agrupadoPorPaciente.size());

            ClassPathResource resource = new ClassPathResource("reportes/sticker_laboratorio_1.jrxml");
            if (!resource.exists()) {
                log.error("❌ Archivo jrxml no existe");
                return ResponseEntity.status(404).body("Reporte no encontrado".getBytes());
            }

            InputStream reportStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            List<JasperPrint> jasperPrintList = new ArrayList<>();

            // POR CADA GRUPO (PACIENTE) GENERAR 1 STICKER
            for (Map.Entry<String, List<Map<String, Object>>> entry : agrupadoPorPaciente.entrySet()) {
                List<Map<String, Object>> exPaciente = entry.getValue();
                Map<String, Object> examenBase = exPaciente.get(0); // para datos de paciente

                String nomPaciente = getStringValue(examenBase, "nomPaciente").toUpperCase();
                Integer edad = getIntValue(examenBase, "edad", 0);
                Integer sexo = getIntValue(examenBase, "sexo", 0);
                String sexoStr = (sexo == 1) ? "M" : "F";
                String edadSexo = edad + "A " + sexoStr;

                String cama = getStringValue(examenBase, "cama", "");
                String prioridad = obtenerLetrasPrioridad(
                        getStringValue(examenBase, "prioridad", "rutinarios"));

                String servicio = getStringValue(examenBase, "servicio", "");
                if (servicio.isEmpty()) {
                    servicio = getStringValue(examenBase, "servicio", "LABORATORIO");
                }
                servicio = servicio.toUpperCase();

                String documento = getStringValue(examenBase, "documento", "");
                String folio = getStringValue(examenBase, "numeroFolio", getStringValue(examenBase, "folio", ""));

                // Construir cadena de abreviaturas de TODOS los exámenes del paciente
                List<String> abreviaturas = new ArrayList<>();
                for (Map<String, Object> ex : exPaciente) {
                    String descProc = getStringValue(ex, "descCups",
                            getStringValue(ex, "descProcedimiento", ""));
                    if (descProc.isEmpty()) continue;
                    String abrev = obtenerAbreviaturaExamen(descProc);
                    if (!abrev.isBlank()) {
                        abreviaturas.add(abrev);
                    }
                }
                String abreviaturaFinal = String.join("_", abreviaturas);
                if (abreviaturaFinal.isBlank()) {
                    abreviaturaFinal = "SIN_ABR";
                }

                log.info("🏷️ Sticker para paciente {} folio {} -> {}", nomPaciente, folio, abreviaturaFinal);

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("PACIENTE_NOMBRE", nomPaciente);
                parameters.put("EDAD_SEXO", edadSexo);
                // AQUÍ va la cadena RCOL_RCER, etc.
                parameters.put("ABREVIATURA_EXAMEN", abreviaturaFinal);
                parameters.put("CAMA",cama);
                parameters.put("PRIORIDAD", prioridad);
                parameters.put("SERVICIO", servicio);
                parameters.put("TIPO_MUESTRA", "LILA / SANGRE TOTAL"); // Podrías derivarlo por tipo si quieres
                parameters.put("FECHA_HC",
                        "FECHA: " + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                parameters.put("DOC", documento);

                JasperPrint jasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        new JREmptyDataSource()
                );
                jasperPrintList.add(jasperPrint);
            }

            if (jasperPrintList.isEmpty()) {
                log.error("❌ No se generaron stickers");
                return ResponseEntity.status(500).body("No se generaron stickers".getBytes());
            }

            JasperPrint masterPrint = jasperPrintList.get(0);
            for (int i = 1; i < jasperPrintList.size(); i++) {
                for (JRPrintPage page : jasperPrintList.get(i).getPages()) {
                    masterPrint.addPage(page);
                }
            }

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(masterPrint);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "stickers-" + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("❌ Error generando PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }


    private String obtenerLetrasPrioridad(String prioridad) {
        if (prioridad == null) return "R";

        String prio = prioridad.toLowerCase();
        //log.info("      prioridad prio: '{}'", prioridad);
        if (prio.contains("muy urgente") || prio.contains("muy_urgentes")) {
            return "MU";
        } else if (prio.contains("urgente") && !prio.contains("muy")) {
            return "U";
        } else if (prio.contains("prioritario") || prio.contains("prioritarios")) {
            return "P";
        } else if (prio.contains("rutinario") || prio.contains("rutinarios")) {
            return "R";
        } else {
            return "NA";
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        return getStringValue(map, key, "");
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value).trim() : defaultValue;
    }

    private Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } catch (Exception e) {
            log.warn("⚠️ No se pudo convertir '{}' a Integer: {}", key, value);
        }
        return defaultValue;
    }
}