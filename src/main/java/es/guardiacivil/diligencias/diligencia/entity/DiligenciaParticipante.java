package es.guardiacivil.diligencias.diligencia.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diligencia_participantes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"diligencia_id", "usuario_id", "rol_participante"})
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DiligenciaParticipante {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diligencia_id", nullable = false)
    private Diligencia diligencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_participante", nullable = false, length = 50)
    private RolParticipante rolParticipante;

    @Column(name = "fecha_asignacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por_id")
    private Usuario asignadoPor;
}
