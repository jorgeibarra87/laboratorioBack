package laboratorio.controller;

import io.swagger.v3.oas.annotations.Operation;
import laboratorio.model.dto.PaginadoDTO;
import laboratorio.model.dto.request.ExamenTomadoRequestDTO;
import laboratorio.model.dto.response.ExamenTomadoResponse;
import laboratorio.service.ExamenesTomadosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j  // ✅ AGREGAR ESTO
@RestController
@RequestMapping("/examenes-tomados")
@RequiredArgsConstructor
public class ExamenesTomadosController {

    private final ExamenesTomadosService examenesTomadosService;

    @Operation(summary = "Obtener examenes tomados por una lista de ingresos",
            description = "Permite obtener los examenes tomados asociados a una lista de ingresos proporcionada en el cuerpo de la solicitud.")
    @PostMapping("/obtener-por-ingresos")
    public ResponseEntity<List<ExamenTomadoResponse>> obtenerPorIngresos(@RequestBody List<Integer> ingresos) {
        log.info("📥 Recibiendo consulta por {} ingresos", ingresos.size());
        List<ExamenTomadoResponse> response = examenesTomadosService.obtenerExamenesTomadosPorIngresos(ingresos);
        log.info("✅ Se encontraron {} exámenes tomados", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear examenes tomados",
            description = "Permite crear nuevos registros de examenes tomados a partir de una lista de solicitudes proporcionada en el cuerpo de la solicitud.")
    @PostMapping
    public ResponseEntity<List<ExamenTomadoResponse>> crear(@Valid @RequestBody List<ExamenTomadoRequestDTO> request) {
        log.info("📥 Recibiendo {} exámenes tomados para guardar", request.size());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.info("📥 Recibiendo {} exámenes tomados para guardar (Usuario: {})", request.size(), username);

        log.info("📥 Recibiendo {} exámenes tomados para guardar (Usuario: {})", request.size(), username);

        // ✅ Log detallado del primer examen (para debug)
        if (!request.isEmpty()) {
            ExamenTomadoRequestDTO primer = request.get(0);
            log.info("  ├─ Primer examen: Paciente={}, Folio={}, FechaSolicitud={}",
                    primer.getNomPaciente(),
                    primer.getNumeroFolio(),
                    primer.getFechaSolicitudFolio());
        }

        List<ExamenTomadoResponse> response = examenesTomadosService.agregarExamenesTomados(request);
        log.info("✅ Se guardaron exitosamente {} exámenes tomados", response.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar examenes tomados con paginación",
            description = "Permite listar todos los examenes tomados de la fecha mas reciente a la antigua con soporte de paginación mediante parámetros de consulta 'page' y 'size'.")
    @GetMapping
    public ResponseEntity<PaginadoDTO<ExamenTomadoResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("📋 Listando exámenes tomados - Page: {}, Size: {}", page, size);
        PaginadoDTO<ExamenTomadoResponse> response = examenesTomadosService.obtenerTodosLosExamenesTomadosPageable(page, size);
        log.info("✅ Retornando {} exámenes totales", response.getTotalElements());
        return ResponseEntity.ok(response);
    }
}
