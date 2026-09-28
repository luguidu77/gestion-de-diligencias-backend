package es.guardiacivil.diligencias.core.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario_tip", nullable = false)
    private String usuarioTip;

    @Column(name = "usuario_nombre")
    private String usuarioNombre;

    @Column(name = "unidad_codigo")
    private String unidadCodigo;

    @Column(nullable = false)
    private String accion;

    private String entidad;

    @Column(name = "entidad_id")
    private String entidadId;

    @Column(name = "resultado", nullable = false, length = 30)
    private String resultado;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detalles; // Almacenado como JSONB en Postgres


}

