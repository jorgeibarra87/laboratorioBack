package laboratorio.service;


import jakarta.validation.Valid;
import laboratorio.model.dto.PaginadoDTO;
import laboratorio.model.dto.request.ExamenTomadoRequestDTO;
import laboratorio.model.dto.response.ExamenTomadoResponse;

import java.util.List;

public interface ExamenesTomadosService {

    List<ExamenTomadoResponse> agregarExamenesTomados(@Valid List<ExamenTomadoRequestDTO> request);
    List<ExamenTomadoResponse> obtenerExamenesTomadosPorIngresos(List<Integer> ingresos);
    PaginadoDTO<ExamenTomadoResponse> obtenerTodosLosExamenesTomadosPageable(int page, int size);
    
}
