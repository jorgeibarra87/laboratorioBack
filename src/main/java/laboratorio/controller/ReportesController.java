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

    @PostMapping("/stickers-pdf")
    public ResponseEntity<byte[]> generarStickersPDF(
            @RequestBody List<Map<String, Object>> examenes) {

        try {
            log.info("📄 Generando PDF de stickers para {} exámenes", examenes.size());

            // ✅ DEBUG: Ver QUÉ DATOS LLEGAN
            if (!examenes.isEmpty()) {
                Map<String, Object> primerExamen = examenes.get(0);
                log.info("🔍 ========== PRIMER EXAMEN COMPLETO ==========");
                primerExamen.forEach((key, value) ->
                        log.info("   {} = {}", key, value)
                );
                log.info("🔍 ============================================");
            }

            ClassPathResource resource = new ClassPathResource("reportes/sticker_laboratorio_1.jrxml");

            if (!resource.exists()) {
                log.error("❌ Archivo no existe");
                return ResponseEntity.status(404).body("Reporte no encontrado".getBytes());
            }

            InputStream reportStream = resource.getInputStream();
            log.info("🔄 Compilando reporte desde .jrxml...");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            log.info("✅ Reporte compilado exitosamente");

            List<JasperPrint> jasperPrintList = new ArrayList<>();

            for (int i = 0; i < examenes.size(); i++) {
                Map<String, Object> examen = examenes.get(i);

                log.info("🏷️ Procesando examen {}/{}", (i + 1), examenes.size());

                try {
                    Map<String, Object> parameters = new HashMap<>();

                    String nomPaciente = getStringValue(examen, "nomPaciente").toUpperCase();
                    Integer edad = getIntValue(examen, "edad", 0);
                    Integer sexo = getIntValue(examen, "sexo", 0);
                    String sexoStr = (sexo == 1) ? "M" : "F";
                    String edadSexo = edad + "A " + sexoStr;
                    String descProcedimiento = getStringValue(examen, "descProcedimiento");
                    String codCama = getStringValue(examen, "codCama", "");
                    String prioridad = obtenerLetrasPrioridad(
                            getStringValue(examen, "prioridad", "rutinarios"));

                    // ✅ Intentar obtener servicio de varios campos posibles
                    String servicio = getStringValue(examen, "areaSolicitante", "");
                    if (servicio.isEmpty()) {
                        servicio = getStringValue(examen, "servicio", "LABORATORIO");
                    }
                    servicio = servicio.toUpperCase();
                    String documento = getStringValue(examen, "documento", "");
                    documento = documento;

                    // ✅ DEBUG: Ver valores construidos
                    log.info("   📋 Valores a usar:");
                    log.info("      nomPaciente: '{}'", nomPaciente);
                    log.info("      edad: {}, sexo: {}, edadSexo: '{}'", edad, sexo, edadSexo);
                    log.info("      descProcedimiento: '{}'", descProcedimiento);
                    log.info("      codCama: '{}'", codCama);
                    log.info("      prioridad: '{}'", prioridad);
                    log.info("      servicio: '{}'", servicio);
                    log.info("      documento: '{}'", documento);

                    parameters.put("PACIENTE_NOMBRE", nomPaciente);
                    parameters.put("EDAD_SEXO", edadSexo);
                    parameters.put("ABREVIATURA_EXAMEN", descProcedimiento.toUpperCase());
                    parameters.put("CAMA", codCama);
                    parameters.put("PRIORIDAD", prioridad);
                    parameters.put("SERVICIO", servicio);
                    parameters.put("TIPO_MUESTRA", "LILA / SANGRE TOTAL");
                    parameters.put("FECHA_HC",
                            "FECHA: " + LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    parameters.put("DOC", documento);

                    JasperPrint jasperPrint = JasperFillManager.fillReport(
                            jasperReport,
                            parameters,
                            new net.sf.jasperreports.engine.JREmptyDataSource()
                    );

                    jasperPrintList.add(jasperPrint);

                } catch (Exception ex) {
                    log.error("❌ Error procesando examen {}: {}", i, ex.getMessage(), ex);
                    ex.printStackTrace();
                    throw ex;
                }
            }

            if (jasperPrintList.isEmpty()) {
                log.error("❌ No se generaron stickers");
                return ResponseEntity.status(500).body("No se generaron stickers".getBytes());
            }

            log.info("📑 Combinando {} stickers...", jasperPrintList.size());
            JasperPrint masterPrint = jasperPrintList.get(0);

            for (int i = 1; i < jasperPrintList.size(); i++) {
                List<JRPrintPage> pages = jasperPrintList.get(i).getPages();
                for (JRPrintPage page : pages) {
                    masterPrint.addPage(page);
                }
            }

            log.info("💾 Exportando a PDF...");
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(masterPrint);

            log.info("✅ PDF generado: {} bytes, {} páginas",
                    pdfBytes.length, jasperPrintList.size());

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
            e.printStackTrace();

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