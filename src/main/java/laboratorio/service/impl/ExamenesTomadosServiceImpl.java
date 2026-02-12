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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public List<ExamenTomadoResponse> obtenerExamenesTomadosPorIngresos(List<String> ingresos) {
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
    @Override
    public List<ExamenTomadoResponse> guardarExamenesImpresion(List<ExamenTomadoRequestDTO> request) {
        LocalDateTime fechaImpresion = LocalDateTime.now();

        List<ExamenesTomados> examenesEntities = request.stream()
                .map(examenRequest -> {
                    ExamenesTomados examenEntity = modelMapper.map(examenRequest, ExamenesTomados.class);
                    examenEntity.setFechaImpresionSticker(fechaImpresion);
                    examenEntity.setFechaTomado(null);
                    examenEntity.setTomadoPor(null);

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

        List<ExamenesTomados> examenesGuardados = examenesTomadosRepository.saveAll(examenesEntities);
        return examenesGuardados.stream()
                .map(examenGuardado -> modelMapper.map(examenGuardado, ExamenTomadoResponse.class))
                .toList();
    }

    @Override
    public List<ExamenTomadoResponse> actualizarExamenesTomados(List<ExamenTomadoRequestDTO> request) {
        LocalDateTime fechaTomado = LocalDateTime.now();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<ExamenesTomados> examenesActualizados = new ArrayList<>();

        for (ExamenTomadoRequestDTO examenRequest : request) {
            // Buscar el examen existente por documento, folio y descCups
            List<ExamenesTomados> examenes = examenesTomadosRepository
                    .findByDocumentoAndFolioAndDescCups(
                            examenRequest.getHistoria(),
                            examenRequest.getNumeroFolio(),
                            examenRequest.getDescCups()
                    );

            if (!examenes.isEmpty()) {
                ExamenesTomados examen = examenes.get(0);
                examen.setFechaTomado(fechaTomado);
                examen.setTomadoPor(username);
                examenesActualizados.add(examen);
            }
        }

        List<ExamenesTomados> guardados = examenesTomadosRepository.saveAll(examenesActualizados);
        return guardados.stream()
                .map(examen -> modelMapper.map(examen, ExamenTomadoResponse.class))
                .toList();
    }


}
