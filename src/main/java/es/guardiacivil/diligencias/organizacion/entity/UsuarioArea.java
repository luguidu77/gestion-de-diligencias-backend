package es.guardiacivil.diligencias.organizacion.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.Instant;

@Entity
@Table(name = "usuarios_areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "area_principal", nullable = false)
    @Builder.Default
    private Boolean areaPrincipal = false;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por_id", nullable = false)
    private Usuario asignadoPor;

    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    @Builder.Default
    private Instant fechaAsignacion = Instant.now();

    @Column(name = "fecha_baja")
    private Instant fechaBaja;
}