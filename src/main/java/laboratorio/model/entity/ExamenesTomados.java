package laboratorio.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "examenes_tomados")
public class ExamenesTomados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EXAMEN_TOMADO")
    private Long id;
    @Column(name = "DOCUMENTO", nullable = false, length = 20)
    private String documento;
    @Column(name = "HISTORIA", nullable = false, length = 20)
    private String historia;
    @Column(name = "NOM_PACIENTE", nullable = false, length = 150)
    private String nomPaciente;
    @Column(name = "SEXO", length = 10)
    private String sexo;
    @Column(name = "EDAD")
    private Integer edad;
    @Column(name = "FOLIO", nullable = false, length = 20)
    private String folio;
    @Column(name = "INGRESO", nullable = false, length = 20)
    private String ingreso;
    @Column(name = "COD_SERVICIO", length = 50)
    private String codServicio;
    @Column(name = "NOM_SERVICIO", nullable = false, length = 255)
    private String nomServicio;
    @Column(name = "COD_CUPS")
    private String codCups;
    @Column(name = "DESC_CUPS", length = 500)
    private String descCups;
    @Column(name = "OBSERVACIONES", columnDefinition = "TEXT")
    private String observaciones;
    @Column(name = "COD_CAMA", length = 20)
    private String codCama;
    @Column(name = "CAMA", length = 100)
    private String cama;
    @Column(name = "DIA_CODIGO", length = 20)
    private String diaCodigo;
    @Column(name = "DIA_NOMBRE", length = 255)
    private String diaNombre;
    @Column(name = "TIPOS_AISLAMIENTO", length = 255)
    private List<String> tiposAislamiento;
    @Column(name = "FECHA_IMPRESION_STICKER")
    private LocalDateTime fechaImpresionSticker;
    @Column(name = "FECHA_SOLICITUD_FOLIO")
    private LocalDateTime fechaSolicitudFolio;
    @Column(name = "AREA_SOLICITANTE", length = 150)
    private String areaSolicitante;
    @Column(name = "PRIORIDAD", length = 50)
    private String prioridad;
    private LocalDateTime fechaTomado;
    private String tomadoPor;

}
