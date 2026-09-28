package es.guardiacivil.diligencias.organizacion.entity;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "unidades")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Unidad extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo;


    @Column(name = "codunidad", nullable = false, unique = true, columnDefinition = "text")
    private String codunidad;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String provincia;

    @Column(length = 20)
    private String telefono;

    @Column(length = 10)
    private String extension;

    @Column(length = 100)
    private String groupwise;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String localidad;

    @Column(length = 10)
    private String codpostal;

    @Column(length = 10)
    private String dir3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_padre_id")
    private Unidad unidadPadre;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

}
