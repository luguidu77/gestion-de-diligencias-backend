package es.guardiacivil.diligencias.diligencia.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actuacion_intervinientes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"actuacion_id", "usuario_id"})
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ActuacionInterviniente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actuacion_id", nullable = false)
    private Actuacion actuacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_en_actuacion", nullable = false, length = 50)
    private RolEnActuacion rolEnActuacion;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
