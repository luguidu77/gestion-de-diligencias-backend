package es.guardiacivil.diligencias.diligencia.controller;

import es.guardiacivil.diligencias.core.audit.AuditableEvent;
import es.guardiacivil.diligencias.core.config.Roles;
import es.guardiacivil.diligencias.diligencia.dto.DiligenciaDTO;
import es.guardiacivil.diligencias.diligencia.dto.DiligenciaCreateRequest;
import es.guardiacivil.diligencias.diligencia.entity.EstadoDiligencia;
import es.guardiacivil.diligencias.diligencia.service.DiligenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diligencias", description = "Gestión de diligencias policiales")
@RestController
@RequestMapping("/api/diligencias")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DiligenciaController {

    private final DiligenciaService diligenciaService;

    @Operation(summary = "Listar diligencias de la unidad")
    @GetMapping
    @PreAuthorize(Roles.TODOS)
    public ResponseEntity<List<DiligenciaDTO>> listarDiligencias() {
        return ResponseEntity.ok(diligenciaService.obtenerDiligenciasPorUnidad());
    }

    @Operation(summary = "Listar diligencias con paginación")
    @GetMapping("/paginado")
    @PreAuthorize(Roles.TODOS)
    public ResponseEntity<Page<DiligenciaDTO>> listarDiligenciasPaginado(Pageable pageable) {
        return ResponseEntity.ok(diligenciaService.obtenerDiligenciasPorUnidadPaginado(pageable));
    }

    @Operation(summary = "Consultar detalle de una diligencia")
    @GetMapping("/{id}")
    @PreAuthorize(Roles.TODOS)
    @AuditableEvent(action = "VER_DETALLE_DILIGENCIA", entity = "diligencias")
    public ResponseEntity<DiligenciaDTO> obtenerDiligencia(@PathVariable Long id) {
        return ResponseEntity.ok(diligenciaService.obtenerDiligenciaDTO(id));
    }

    @Operation(summary = "Crear una nueva diligencia")
    @PostMapping
    @PreAuthorize(Roles.PUEDE_ESCRIBIR)
    @AuditableEvent(action = "CREAR_DILIGENCIA", entity = "diligencias")
    public ResponseEntity<DiligenciaDTO> crearDiligencia(@Valid @RequestBody DiligenciaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diligenciaService.crearDiligencia(request));
    }

    @Operation(summary = "Modificar una diligencia")
    @PutMapping("/{id}")
    @PreAuthorize(Roles.PUEDE_ESCRIBIR)
    @AuditableEvent(action = "MODIFICAR_DILIGENCIA", entity = "diligencias")
    public ResponseEntity<DiligenciaDTO> actualizarDiligencia(
            @PathVariable Long id,
            @Valid @RequestBody DiligenciaCreateRequest request) {
        return ResponseEntity.ok(diligenciaService.actualizarDiligencia(id, request));
    }

    @Operation(summary = "Cambiar estado de diligencia")
    @PatchMapping("/{id}/estado")
    @PreAuthorize(Roles.PUEDE_ESCRIBIR)
    @AuditableEvent(action = "CAMBIAR_ESTADO_DILIGENCIA", entity = "diligencias")
    public ResponseEntity<DiligenciaDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoDiligencia estado) {
        return ResponseEntity.ok(diligenciaService.cambiarEstado(id, estado));
    }

    @Operation(summary = "Eliminar una diligencia")
    @DeleteMapping("/{id}")
    @PreAuthorize(Roles.PUEDE_ELIMINAR)
    @AuditableEvent(action = "ELIMINAR_DILIGENCIA", entity = "diligencias")
    public ResponseEntity<Void> eliminarDiligencia(@PathVariable Long id) {
        diligenciaService.eliminarDiligencia(id);
        return ResponseEntity.noContent().build();
    }
}
