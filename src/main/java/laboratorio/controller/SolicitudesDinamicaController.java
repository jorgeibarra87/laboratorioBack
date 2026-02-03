package laboratorio.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dinamica-microservicio/hcnSolExa")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PermitAll
@RequiredArgsConstructor
public class SolicitudesDinamicaController {

    @Qualifier("externalJdbcTemplate")
    private final JdbcTemplate externalJdbcTemplate;

    // ✅ MAPEO SIMPLE DE ESTADOS
    private static final Map<String, String> ESTADOS_MAP = Map.of(
            "urgentes", "0",
            "rutinarios", "1",
            "prioritarios", "3",
            "muy_urgentes", "4"
    );

    @GetMapping("/resumen-pacientes")
    public ResponseEntity<Map<String, Object>> resumenPacientes(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        log.info("📥 Solicitud - tipo: {}, page: {}, size: {}", tipo, page, size);

        try {
            // ✅ Obtener estado desde el mapa
            String estado = ESTADOS_MAP.getOrDefault(tipo.toLowerCase(), "0");

            // ✅ Consultar BD
            List<Map<String, Object>> pacientes = consultarPacientes(estado);

            log.info("✅ Obtenidos {} pacientes", pacientes.size());

            // ✅ Paginación simple
            return ResponseEntity.ok(paginar(pacientes, page, size));

        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.ok(respuestaVacia(page, size));
        }
    }

    /**
     * ✅ CONSULTA
     */
    private List<Map<String, Object>> consultarPacientes(String estado) {

        String sql = """
            SELECT 
                E.PACNUMDOC AS documento,
                E.GPANOMCOM AS nomPaciente,
                E.GPASEXPAC AS sexo,
                FLOOR(DATEDIFF(DAY, E.GPAFECNAC, GETDATE()) / 365.25) AS edad,
                C.AINCONSEC AS ingreso,
                F.HCNUMFOL AS folio,
                D.HCACODIGO AS codCama,
                D.HCANOMBRE AS cama,
                G.GASCODIGO + ' - ' + G.GASNOMBRE AS areaSolicitante,
                B.SIPCODCUP AS codCups,
                B.SIPDESCUP AS descProcedimiento,
                ISNULL(A.HCSOBSERV, '') AS observaciones,
                F.HCFECFOL AS fechaFolioSolicitud,
                dbo.GENDIAGNO.DIACODIGO AS diaCodigo,
                dbo.GENDIAGNO.DIANOMBRE AS diaNombre,
                CASE dbo.HCNINDMED.HCITIPAIS
                    WHEN '0' THEN 'Precaucion_Estandar'
                    WHEN '1' THEN 'Precaucion_Vias_Aereas'
                    WHEN '2' THEN 'Precaucion_por_Gotas'
                    WHEN '3' THEN 'Precaucion_por_Contacto'
                    WHEN '4' THEN 'Aislamiento_en_Cohorte'
                    WHEN '5' THEN 'Ambiente_Protector'
                    WHEN '6' THEN 'Transmision_por_Vectores'
                END AS tipoAislamiento,
                dbo.HPNSUBGRU.HSUNOMBRE AS servicio
            FROM dbo.HCNSOLEXA AS A WITH (NOLOCK)
            INNER JOIN dbo.GENSERIPS AS B WITH (NOLOCK) ON A.GENSERIPS = B.OID
            INNER JOIN dbo.ADNINGRESO AS C WITH (NOLOCK) ON A.ADNINGRESO = C.OID
            INNER JOIN dbo.HPNDEFCAM AS D WITH (NOLOCK) ON C.HPNDEFCAM = D.OID
            INNER JOIN dbo.GENPACIEN AS E WITH (NOLOCK) ON C.GENPACIEN = E.OID
            INNER JOIN dbo.HCNFOLIO AS F WITH (NOLOCK) ON A.HCNFOLIO = F.OID
            INNER JOIN dbo.GENARESER AS G WITH (NOLOCK) ON F.GENARESER = G.OID
            INNER JOIN dbo.HCNDIAPAC ON F.OID = dbo.HCNDIAPAC.HCNFOLIO
            INNER JOIN dbo.GENDIAGNO ON dbo.HCNDIAPAC.GENDIAGNO = dbo.GENDIAGNO.OID
            INNER JOIN dbo.HCNINDMED ON F.HCNINDMED = dbo.HCNINDMED.OID 
                AND F.OID = dbo.HCNINDMED.HCNFOLIO
            INNER JOIN dbo.HPNSUBGRU ON D.HPNSUBGRU = dbo.HPNSUBGRU.OID
            WHERE F.HCFECFOL >= DATEADD(day, -30, GETDATE())
                AND C.AINESTADO = 0
                AND A.HCSESTADO = ?
                AND A.HCNRESEXA IS NULL
                AND dbo.HCNDIAPAC.HCPDIAPRIN = 1
                AND dbo.HPNSUBGRU.HSUNOMBRE LIKE N'%interm%'
            ORDER BY F.HCFECFOL DESC
        """;

        // ✅ Ejecutar query con un solo parámetro
        List<Map<String, Object>> rows = externalJdbcTemplate.queryForList(sql, estado);

        log.info("📊 Registros obtenidos: {}", rows.size());

        // ✅ Agrupar por paciente
        return agruparPorPaciente(rows);
    }

    /**
     * ✅ AGRUPACIÓN
     */
    private List<Map<String, Object>> agruparPorPaciente(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return new ArrayList<>();

        // Agrupar por documento + folio
        Map<String, List<Map<String, Object>>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> row.get("documento") + "-" + row.get("folio"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.values().stream()
                .map(this::crearPaciente)
                .collect(Collectors.toList());
    }

    /**
     * ✅ CREAR OBJETO PACIENTE
     */
    private Map<String, Object> crearPaciente(List<Map<String, Object>> examenes) {
        Map<String, Object> primero = examenes.get(0);

        Map<String, Object> paciente = new HashMap<>();

        // Datos del paciente
        paciente.put("documento", primero.get("documento"));
        paciente.put("nomPaciente", primero.get("nomPaciente"));
        paciente.put("sexo", primero.get("sexo"));
        paciente.put("edad", primero.get("edad"));
        paciente.put("ingreso", primero.get("ingreso"));
        paciente.put("codCama", primero.get("codCama"));
        paciente.put("cama", primero.get("cama"));
        paciente.put("diaCodigo", primero.get("diaCodigo"));
        paciente.put("diaNombre", primero.get("diaNombre"));
        paciente.put("areaSolicitante", primero.get("areaSolicitante"));

        // Tipos de aislamiento
        Object tipoAislamiento = primero.get("tipoAislamiento");
        paciente.put("tiposAislamiento",
                tipoAislamiento != null && !tipoAislamiento.toString().isBlank()
                        ? List.of(tipoAislamiento.toString())
                        : List.of("Precaucion_Estandar")
        );

        // Array de exámenes
        paciente.put("examenes", examenes.stream()
                .map(this::simplificarExamen)
                .collect(Collectors.toList())
        );

        return paciente;
    }

    /**
     * ✅ EXAMEN
     */
    private Map<String, Object> simplificarExamen(Map<String, Object> examen) {
        return Map.of(
                "folio", examen.get("folio"),
                "codCups", examen.get("codCups"),
                "descProcedimiento", examen.get("descProcedimiento"),
                "observaciones", examen.get("observaciones"),
                "fechaFolioSolicitud", examen.get("fechaFolioSolicitud"),
                "codCama", examen.get("codCama"),
                "cama", examen.get("cama"),
                "areaSolicitante", examen.get("areaSolicitante")
        );
    }

    /**
     * ✅ PAGINACIÓN
     */
    private Map<String, Object> paginar(List<Map<String, Object>> datos, int page, int size) {
        int total = datos.size();
        int start = page * size;
        int end = Math.min(start + size, total);

        List<Map<String, Object>> paginados = start < total
                ? datos.subList(start, end)
                : new ArrayList<>();

        return Map.of(
                "content", paginados,
                "totalElements", (long) total,
                "totalPages", (int) Math.ceil((double) total / size),
                "size", size,
                "number", page,
                "first", page == 0,
                "last", end >= total,
                "numberOfElements", paginados.size(),
                "empty", paginados.isEmpty()
        );
    }

    /**
     * ✅ RESPUESTA VACÍA
     */
    private Map<String, Object> respuestaVacia(int page, int size) {
        return Map.of(
                "content", new ArrayList<>(),
                "totalElements", 0L,
                "totalPages", 0,
                "size", size,
                "number", page,
                "first", true,
                "last", true,
                "numberOfElements", 0,
                "empty", true
        );
    }
}
