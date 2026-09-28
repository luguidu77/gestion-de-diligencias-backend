package es.guardiacivil.diligencias.diligencia.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "historico_estados_diligencia")
@Immutable
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HistoricoEstadoDiligencia {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diligencia_id", nullable = false)
    private Diligencia diligencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior")
    private EstadoDiligencia estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false)
    private EstadoDiligencia estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_cambio", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCambio = LocalDateTime.now();
}
