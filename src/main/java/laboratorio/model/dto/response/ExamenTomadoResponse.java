package laboratorio.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ExamenTomadoResponse {

    private Long id;
    private String documento;
    private String historia;
    private String nomPaciente;
    private String sexo;
    private Integer edad;
    private String folio;
    private String ingreso;
    private String codServicio;
    private String nomServicio;
    private String codCups;
    private String descCups;
    private String observaciones;
    private String codCama;
    private String cama;
    private String diaCodigo;
    private String diaNombre;
    private List<String> tiposAislamiento;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime fechaSolicitudFolio;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime fechaTomado;
    private String areaSolicitante;
    private String prioridad;
    private String tomadoPor;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime fechaImpresionSticker;
}
