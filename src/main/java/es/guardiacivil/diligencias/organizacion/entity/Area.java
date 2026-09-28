package es.guardiacivil.diligencias.organizacion.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "areas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Area extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;
}