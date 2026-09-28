package es.guardiacivil.diligencias.outbox.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_eventos")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OutboxEvento {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "jsonb", nullable = false)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEventoOutbox estado;

    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "proximo_reintento")
    private LocalDateTime proximoReintento;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
