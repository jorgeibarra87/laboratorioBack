package laboratorio.repository;

import laboratorio.model.entity.ExamenesTomados;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamenesTomadosRepository extends JpaRepository<ExamenesTomados, Long> {

    List<ExamenesTomados> findByIngresoIn(List<String> ingresos);
    Page<ExamenesTomados> findAllByOrderByFechaTomadoDesc(Pageable pageable);

}
