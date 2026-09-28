package es.guardiacivil.diligencias.usuario.entity;

import es.guardiacivil.diligencias.organizacion.entity.Area;

import es.guardiacivil.diligencias.organizacion.entity.UsuarioArea;

import es.guardiacivil.diligencias.organizacion.entity.Unidad;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "keycloak_subject", unique = true, nullable = false)
    private UUID keycloakSubject = UUID.randomUUID();

    @Column(name = "tip", unique = true, nullable = false, length = 20)
    private String tip;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String empleo;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaAlta = LocalDateTime.now();

    // Lista de todas las Ã¡reas (histÃ³ricas y actuales) a las que ha sido asignado el usuario
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<UsuarioArea> areas = new java.util.ArrayList<>();


}
