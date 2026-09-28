package es.guardiacivil.diligencias.batch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "sincronizaciones_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SincronizacionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_proceso", nullable = false, unique = true, length = 100)
    private String nombreProceso;

    @Column(name = "fecha_ultima_importacion", nullable = false)
    private Instant fechaUltimaImportacion;

    @Column(name = "registros_procesados", nullable = false)
    @Builder.Default
    private Integer registrosProcesados = 0;

    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private String estado = "EXITO";

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private Instant fechaInicio = Instant.now();

    @Column(name = "fecha_fin")
    private Instant fechaFin;
}
