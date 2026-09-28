package es.guardiacivil.diligencias.diligencia.entity;

import es.guardiacivil.diligencias.organizacion.entity.Unidad;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diligencias", indexes = {
    @Index(name = "idx_diligencias_unidad_fecha", columnList = "unidad_id, fecha_creacion DESC")
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
public class Diligencia extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_expediente", unique = true, nullable = false, columnDefinition = "text")
    private String numeroExpediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_instructor_id")
    private Usuario agenteInstructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_secretario_id")
    private Usuario agenteSecretario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDiligencia estado;

    @Column(name = "delito_principal", length = 200)
    private String delitoPrincipal;

    @Enumerated(EnumType.STRING)
    @Column(name = "gravedad_delito", length = 20)
    private GravedadDelito gravedadDelito;

    @Column(name = "anios_retencion_legal")
    private Integer aniosRetencionLegal;

    @Column(name = "fecha_expiracion_retencion")
    private LocalDate fechaExpiracionRetencion;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Version
    private Long version;

    public static String generarNumeroExpedienteOficial(String codunidad, long count) {
        return String.format("%d-%s-%07d", LocalDate.now().getYear(), codunidad, count);
    }
}
