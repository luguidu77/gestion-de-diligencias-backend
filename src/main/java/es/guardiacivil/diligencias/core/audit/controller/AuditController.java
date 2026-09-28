package es.guardiacivil.diligencias.core.audit.controller;

import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import es.guardiacivil.diligencias.core.config.Roles;
import es.guardiacivil.diligencias.core.audit.entity.AuditLog;
import es.guardiacivil.diligencias.core.audit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AuditorÃ­a Legal", description = "Consulta del registro inmutable de trazabilidad forense de operaciones")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize(Roles.PUEDE_AUDITAR)
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final AuthenticatedUserService usuarioActualService;

    @Operation(
            summary = "Consultar registros de auditorÃ­a",
            description = "Devuelve el historial inmutable de acciones auditadas. ADMIN_UNIDAD solo puede consultar los registros de su unidad. SUPERADMIN puede consultar todas o filtrar por unidad. Requiere rol ADMIN_UNIDAD o SUPERADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs de auditorÃ­a recuperados"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invÃ¡lido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN_UNIDAD o SUPERADMIN)")
    })
    @GetMapping
    public ResponseEntity<List<AuditLog>> obtenerLogsAuditoria(
            @Parameter(description = "CÃ³digo de unidad para filtrar (solo permitido a SUPERADMIN)", example = "PJ-LUG-01")
            @RequestParam(required = false) String unidad) {

        String codigoUnidad = resolverCodigoUnidad(unidad);

        if (codigoUnidad != null) {
            return ResponseEntity.ok(auditLogRepository.findByUnidadCodigo(codigoUnidad));
        }
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    @Operation(
            summary = "Consultar registros de auditorÃ­a paginados",
            description = "VersiÃ³n paginada del listado de auditorÃ­a con idÃ©nticos criterios de seguridad por unidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PÃ¡gina de auditorÃ­a recuperada"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invÃ¡lido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/paginado")
    public ResponseEntity<Page<AuditLog>> obtenerLogsAuditoriaPaginado(
            @Parameter(description = "CÃ³digo de unidad para filtrar", example = "PJ-LUG-01")
            @RequestParam(required = false) String unidad,
            Pageable pageable) {

        String codigoUnidad = resolverCodigoUnidad(unidad);

        if (codigoUnidad != null) {
            return ResponseEntity.ok(auditLogRepository.findByUnidadCodigo(codigoUnidad, pageable));
        }
        return ResponseEntity.ok(auditLogRepository.findAll(pageable));
    }

    private String resolverCodigoUnidad(String unidadParam) {
        if (!usuarioActualService.isSuperadmin()) {
            if (unidadParam != null && !unidadParam.isBlank() && !unidadParam.equals(usuarioActualService.getUnitCode())) {
                throw new org.springframework.security.access.AccessDeniedException("No tiene permisos para ver auditorÃ­a de otra unidad.");
            }
            return usuarioActualService.getUnitCode();
        }
        return (unidadParam != null && !unidadParam.isBlank()) ? unidadParam : null;
    }
}

