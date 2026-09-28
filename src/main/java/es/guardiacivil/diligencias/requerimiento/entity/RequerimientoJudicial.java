package es.guardiacivil.diligencias.requerimiento.entity;

import es.guardiacivil.diligencias.diligencia.entity.Diligencia;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Requerimiento judicial recibido de la aplicación judicial externa.
 *
 * <p>Cada requerimiento está vinculado a un {@link Expediente} policial
 * y tiene un ciclo de vida propio gestionado mediante una máquina de estados cerrada.</p>
 *
 * <p>El campo {@code identificadorExterno} garantiza idempotencia:
 * si la aplicación judicial envía el mismo requerimiento dos veces con el mismo
 * identificador, el backend devuelve el existente sin crear un duplicado.</p>
 */
@Entity
@Table(
    name = "requerimientos_judiciales",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_requerimiento_origen_externo",
            columnNames = {"sistema_origen", "identificador_externo"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequerimientoJudicial extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Diligencia policial a la que pertenece este requerimiento.
     * No se puede cambiar una vez asignada.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diligencia_id", nullable = false)
    private Diligencia diligencia;

    /** Referencia del órgano judicial (NIG, número de autos, etc.). */
    @Column(name = "referencia_judicial", nullable = false)
    private String referenciaJudicial;

    /**
     * Identificador único generado por la aplicación judicial.
     * Sirve para garantizar idempotencia: misma petición = misma respuesta.
     */
    @Column(name = "identificador_externo", nullable = false)
    private String identificadorExterno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRequerimientoJudicial tipo;

    @Column(nullable = false, length = 4000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadRequerimiento prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRequerimientoJudicial estado;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(name = "fecha_recepcion", nullable = false)
    private Instant fechaRecepcion;

    @Column(name = "fecha_asignacion")
    private Instant fechaAsignacion;

    @Column(name = "fecha_respuesta")
    private Instant fechaRespuesta;

    /** TIP del instructor al que se asigna el requerimiento (del campo usuarioAsignado del Expediente). */
    @Column(name = "instructor_tip")
    private String instructorTip;

    @Column(name = "instructor_nombre")
    private String instructorNombre;

    @Column(name = "unidad_responsable")
    private String unidadResponsable;

    /**
     * Sistema que originó la petición.
     */
    @Column(name = "sistema_origen", nullable = false)
    private String sistemaOrigen;

    @Column(length = 2000)
    private String resultado;

    @Column(length = 2000)
    private String observaciones;

    /** Control de concurrencia optimista. */
    @Version
    private Long version;
}
