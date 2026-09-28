package es.guardiacivil.diligencias.delegacion.entity;

import es.guardiacivil.diligencias.diligencia.entity.Diligencia;

import es.guardiacivil.diligencias.organizacion.entity.Unidad;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delegaciones")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
public class Delegacion extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_delegante_id", nullable = false)
    private Usuario usuarioDelegante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_delegado_id", nullable = false)
    private Usuario usuarioDelegado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diligencia_id")
    private Diligencia diligencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_delegacion", nullable = false)
    private TipoDelegacion tipoDelegacion;

    @Column(length = 250)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id")
    private Usuario creadoPorUsuario;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
