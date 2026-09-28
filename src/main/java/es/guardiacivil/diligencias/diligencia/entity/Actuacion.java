package es.guardiacivil.diligencias.diligencia.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actuaciones", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"diligencia_id", "numero_orden"})
})
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Actuacion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diligencia_id", nullable = false)
    private Diligencia diligencia;

    @Column(name = "numero_orden", nullable = false)
    private Integer numeroOrden;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actuacion", nullable = false, length = 100)
    private TipoActuacion tipoActuacion;

    @Column(length = 200)
    private String lugar;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creador_id")
    private Usuario usuarioCreador;

    @Column(name = "fecha_actuacion", nullable = false)
    private LocalDateTime fechaActuacion;
}
