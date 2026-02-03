package laboratorio.service.impl;

import jakarta.annotation.security.PermitAll;
import laboratorio.model.dto.PaginadoDTO;
import laboratorio.model.dto.request.ExamenTomadoRequestDTO;
import laboratorio.model.dto.response.ExamenTomadoResponse;
import laboratorio.model.entity.ExamenesTomados;
import laboratorio.repository.ExamenesTomadosRepository;
import laboratorio.service.ExamenesTomadosService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@PermitAll
public class ExamenesTomadosServiceImpl implements ExamenesTomadosService {

    private final ModelMapper modelMapper;
    private final ExamenesTomadosRepository examenesTomadosRepository;

    @Override
    public List<ExamenTomadoResponse> agregarExamenesTomados(List<ExamenTomadoRequestDTO> request) {
        // Asignar la fecha y hora actual a cada examen tomado antes de guardarlo
        LocalDateTime fechaTomado = LocalDateTime.now();
        // Creamos las entidades a partir de los DTOs de solicitud
        List<ExamenesTomados> examenesEntities = request.stream()
                .map(examenRequest -> {
                    ExamenesTomados examenEntity = modelMapper.map(examenRequest, ExamenesTomados.class);
                    examenEntity.setFechaTomado(fechaTomado);
                    if(examenEntity.getCodServicio() == null || examenEntity.getCodServicio().isBlank()){
                        if (examenEntity.getDescCups() != null && !examenEntity.getDescCups().isBlank()) {
                            examenEntity.setNomServicio(examenEntity.getDescCups());
                        } else {
                            examenEntity.setNomServicio("LABORATORIO");
                        }
                    }
                    return examenEntity;
                })
                .toList();
        // Guardamos todas las entidades en la base de datos
        List<ExamenesTomados> examenesGuardados = examenesTomadosRepository.saveAll(examenesEntities);
        return examenesGuardados.stream()
                .map(examenGuardado -> modelMapper.map(examenGuardado, ExamenTomadoResponse.class))
                .toList();
    }

    @Override
    public List<ExamenTomadoResponse> obtenerExamenesTomadosPorIngresos(List<Integer> ingresos) {
        List<ExamenesTomados> examenes = examenesTomadosRepository.findByIngresoIn(ingresos);
        return examenes.stream()
                .map(examen -> modelMapper.map(examen, ExamenTomadoResponse.class))
                .toList();
    }

    @Override
    public PaginadoDTO<ExamenTomadoResponse> obtenerTodosLosExamenesTomadosPageable(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<ExamenesTomados> examenesTomados = examenesTomadosRepository.findAllByOrderByFechaTomadoDesc(pageable);
        Page<ExamenTomadoResponse> responsePage = examenesTomados.map(examen -> modelMapper.map(examen, ExamenTomadoResponse.class));
        return new PaginadoDTO<>(responsePage);
    }

}
