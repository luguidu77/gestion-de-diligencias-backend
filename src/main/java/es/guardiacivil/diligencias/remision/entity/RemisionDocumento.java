package es.guardiacivil.diligencias.remision.entity;

import es.guardiacivil.diligencias.documento.entity.DocumentoMetadata;

import es.guardiacivil.diligencias.core.audit.entity.Auditable;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "remisiones_documento",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_remision_referencia_externa",
            columnNames = {
                "sistema_destino",
                "referencia_externa"
            }
        ),
        @UniqueConstraint(
            name = "uk_remision_idempotencia",
            columnNames = {
                "clave_idempotencia"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemisionDocumento extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoMetadata documento;

    @Column(name = "sistema_destino", nullable = false)
    private String sistemaDestino;

    @Column(name = "destinatario", nullable = false)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoRemision estado;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Column(name = "destino", nullable = false)
    private String destino;

    @Column(name = "solicitada_por", nullable = false)
    private String solicitadaPor;

    @Column(name = "ejecutada_por")
    private String ejecutadaPor;

    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_remision", nullable = false)
    private TipoRemision tipoRemision;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;

    @Column(name = "numero_intentos", nullable = false)
    private Integer numeroIntentos;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    @Column(name = "proximo_reintento")
    private Instant proximoReintento;

    @Column(name = "clave_idempotencia", unique = true, nullable = false)
    private String claveIdempotencia;

    @Version
    private Long version;
}
