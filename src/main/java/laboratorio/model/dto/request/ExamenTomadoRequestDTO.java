package laboratorio.model.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import laboratorio.config.FlexibleLocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ExamenTomadoRequestDTO {

    @NotBlank(message = "La historia es obligatoria")
    @JsonProperty("historia")
    @JsonAlias({"documento", "historia"})
    private String historia;
    @JsonProperty("documento")
    private String documento;
    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String nomPaciente;
    private String sexo;
    private Integer edad;
    @NotBlank(message = "El número de folio es obligatorio")
    @JsonProperty("numeroFolio")
    @JsonAlias({"folio", "numeroFolio"})
    private String numeroFolio;
    @NotBlank(message = "El número de ingreso es obligatorio")
    @JsonProperty("numeroIngreso")
    @JsonAlias({"ingreso", "numeroIngreso"})
    private String numeroIngreso;
    @JsonProperty("codServicio")
    private String codServicio;
    @JsonProperty("nomServicio")
    @JsonAlias({"servicio", "nomServicio"})
    private String nomServicio;
    @JsonProperty("codCups")
    private String codCups;
    @JsonProperty("descCups")
    @JsonAlias({"descProcedimiento", "descCups"})
    private String descCups;
    private String observaciones;
    @JsonProperty("codCama")
    private String codCama;
    @JsonProperty("cama")
    private String cama;
    private String diaCodigo;
    private String diaNombre;
    private List<String> tiposAislamiento;
    @JsonProperty("fechaSolicitud")
    @JsonAlias({"fechaSolicitudFolio", "fechaSolicitud"})
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime fechaSolicitudFolio;
    private String areaSolicitante;
    private String prioridad;

}
