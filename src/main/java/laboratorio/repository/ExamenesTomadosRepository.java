package laboratorio.repository;

import laboratorio.model.entity.ExamenesTomados;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamenesTomadosRepository extends JpaRepository<ExamenesTomados, Long> {

    List<ExamenesTomados> findByIngresoIn(List<String> ingresos);
    Page<ExamenesTomados> findAllByOrderByFechaTomadoDesc(Pageable pageable);
    //List<ExamenesTomados> findByDocumentoAndFolioAndDescCups(String documento, String folio, String descCups);
    @Query("SELECT e FROM ExamenesTomados e WHERE e.documento = :documento AND e.folio = :folio AND e.descCups = :descCups")
    List<ExamenesTomados> findByDocumentoAndFolioAndDescCups(
            @Param("documento") String documento,
            @Param("folio") String folio,
            @Param("descCups") String descCups
    );

}
