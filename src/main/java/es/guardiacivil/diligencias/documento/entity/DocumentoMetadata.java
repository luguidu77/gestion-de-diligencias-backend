package es.guardiacivil.diligencias.documento.entity;

import es.guardiacivil.diligencias.diligencia.entity.Diligencia;

import es.guardiacivil.diligencias.diligencia.entity.Actuacion;

import es.guardiacivil.diligencias.usuario.entity.Usuario;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documentos_metadata")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = false)
public class DocumentoMetadata extends Auditable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diligencia_id", nullable = false)
    private Diligencia diligencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actuacion_id")
    private Actuacion actuacion;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "titulo_documento", length = 200)
    private String tituloDocumento;

    @Column(name = "alfresco_node_id", unique = true, length = 64)
    private String alfrescoNodeId;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DestinoDocumento destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_documento", length = 50)
    private EstadoDocumento estadoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_almacenamiento", length = 50)
    private EstadoAlmacenamiento estadoAlmacenamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por_id")
    private Usuario subidoPor;

    @Version
    private Long version;
}
