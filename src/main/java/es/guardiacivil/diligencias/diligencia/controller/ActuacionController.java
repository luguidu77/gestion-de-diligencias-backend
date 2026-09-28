package es.guardiacivil.diligencias.diligencia.controller;

import es.guardiacivil.diligencias.core.audit.AuditableEvent;
import es.guardiacivil.diligencias.core.config.Roles;
import es.guardiacivil.diligencias.diligencia.dto.ActuacionDTO;
import es.guardiacivil.diligencias.diligencia.dto.ActuacionCreateRequest;
import es.guardiacivil.diligencias.diligencia.service.ActuacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Actuaciones", description = "Actuaciones dentro de una diligencia policial")
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ActuacionController {

    private final ActuacionService actuacionService;

    @Operation(summary = "Listar actuaciones de una diligencia")
    @GetMapping("/api/diligencias/{idDiligencia}/actuaciones")
    @PreAuthorize(Roles.TODOS)
    public ResponseEntity<List<ActuacionDTO>> listarActuaciones(@PathVariable Long idDiligencia) {
        return ResponseEntity.ok(actuacionService.obtenerActuacionesPorDiligencia(idDiligencia));
    }

    @Operation(summary = "Crear nueva actuacion en diligencia")
    @PostMapping("/api/diligencias/{idDiligencia}/actuaciones")
    @PreAuthorize(Roles.PUEDE_ESCRIBIR)
    @AuditableEvent(action = "CREAR_ACTUACION", entity = "actuaciones")
    public ResponseEntity<ActuacionDTO> crearActuacion(
            @PathVariable Long idDiligencia,
            @Valid @RequestBody ActuacionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actuacionService.crearActuacion(idDiligencia, request));
    }

    @Operation(summary = "Modificar una actuacion")
    @PutMapping("/api/actuaciones/{id}")
    @PreAuthorize(Roles.PUEDE_ESCRIBIR)
    @AuditableEvent(action = "MODIFICAR_ACTUACION", entity = "actuaciones")
    public ResponseEntity<ActuacionDTO> actualizarActuacion(
            @PathVariable Long id,
            @Valid @RequestBody ActuacionCreateRequest request) {
        return ResponseEntity.ok(actuacionService.actualizarActuacion(id, request));
    }

    @Operation(summary = "Eliminar una actuacion")
    @DeleteMapping("/api/actuaciones/{id}")
    @PreAuthorize(Roles.PUEDE_ELIMINAR)
    @AuditableEvent(action = "ELIMINAR_ACTUACION", entity = "actuaciones")
    public ResponseEntity<Void> eliminarActuacion(@PathVariable Long id) {
        actuacionService.eliminarActuacion(id);
        return ResponseEntity.noContent().build();
    }
}
