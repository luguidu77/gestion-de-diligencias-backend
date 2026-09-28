package es.guardiacivil.diligencias.requerimiento.controller;

import es.guardiacivil.diligencias.config.AuthenticatedClient;
import es.guardiacivil.diligencias.config.AuthenticatedClientService;
import es.guardiacivil.diligencias.core.config.Roles;
import es.guardiacivil.diligencias.requerimiento.dto.CrearRequerimientoJudicialRequest;
import es.guardiacivil.diligencias.diligencia.dto.EstadoDiligenciaJudicialResponse;
import es.guardiacivil.diligencias.requerimiento.dto.RequerimientoJudicialResponse;
import es.guardiacivil.diligencias.requerimiento.service.RequerimientoJudicialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Integración Judicial", description = "Endpoints técnicos para consumo exclusivo por aplicaciones del órgano judicial (OAuth2 client_credentials)")
@RestController
@RequestMapping("/api/integraciones/judicial")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class IntegracionJudicialController {

    private final RequerimientoJudicialService requerimientoService;
    private final AuthenticatedClientService clientService;

    @Operation(
            summary = "Consultar estado de diligencias policiales",
            description = "Devuelve el estado público y no sensible de unas diligencias a partir de la referencia del órgano judicial. Requiere rol técnico CONSULTAR_ESTADO_JUDICIAL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de las diligencias encontrado"),
            @ApiResponse(responseCode = "401", description = "JWT técnico ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Cliente sin rol técnico CONSULTAR_ESTADO_JUDICIAL"),
            @ApiResponse(responseCode = "404", description = "No existe expediente con la referencia judicial indicada")
    })
    @GetMapping("/diligencias/{referenciaJudicial}/estado")
    @PreAuthorize(Roles.PUEDE_CONSULTAR_JUDICIAL)
    public ResponseEntity<EstadoDiligenciaJudicialResponse> consultarEstado(
            @Parameter(description = "Referencia del órgano judicial (NIG o autos)", example = "NIG-27028-43-2026")
            @PathVariable String referenciaJudicial) {

        AuthenticatedClient client = clientService.getAuthenticatedClient();
        log.info("Cliente '{}' consulta estado de diligencias para refJudicial='{}'",
                client.clientId(), referenciaJudicial);

        EstadoDiligenciaJudicialResponse response =
                requerimientoService.consultarEstado(referenciaJudicial, client.clientId());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Recepcionar requerimiento judicial",
            description = "Recibe un requerimiento judicial emitido por el juzgado. Es de carácter IDEMPOTENTE: si ya existe el identificadorExterno, devuelve 200 OK con el existente sin duplicar. Requiere rol técnico ENVIAR_REQUERIMIENTOS_JUDICIALES."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nuevo requerimiento creado y asignado al instructor policial"),
            @ApiResponse(responseCode = "200", description = "Requerimiento existente devuelto (Respuesta idempotente)"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada no válidos"),
            @ApiResponse(responseCode = "401", description = "JWT técnico ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Cliente sin rol técnico ENVIAR_REQUERIMIENTOS_JUDICIALES"),
            @ApiResponse(responseCode = "404", description = "Referencia policial no encontrada")
    })
    @PostMapping("/requerimientos")
    @PreAuthorize(Roles.PUEDE_ENVIAR_JUDICIAL)
    public ResponseEntity<RequerimientoJudicialResponse> crearRequerimiento(
            @Valid @RequestBody CrearRequerimientoJudicialRequest request) {

        AuthenticatedClient client = clientService.getAuthenticatedClient();
        log.info("Cliente '{}' envía requerimiento judicial. idExterno='{}'",
                client.clientId(), request.identificadorExterno());

        boolean esNuevo = !requerimientoService.existeRequerimiento(request.identificadorExterno());

        RequerimientoJudicialResponse response =
                requerimientoService.crearRequerimiento(request, client.clientId());

        return esNuevo
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar respuesta o avance de un requerimiento",
            description = "Permite a la aplicación judicial consultar el estado de tramitación o respuesta emitida a su requerimiento. Requiere rol técnico CONSULTAR_RESPUESTAS_JUDICIALES."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Requerimiento encontrado"),
            @ApiResponse(responseCode = "401", description = "JWT técnico ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Cliente sin rol técnico CONSULTAR_RESPUESTAS_JUDICIALES"),
            @ApiResponse(responseCode = "404", description = "Requerimiento no encontrado")
    })
    @GetMapping("/requerimientos/{id}")
    @PreAuthorize(Roles.PUEDE_CONSULTAR_RESPUESTAS_JUDICIALES)
    public ResponseEntity<RequerimientoJudicialResponse> consultarRequerimiento(
            @Parameter(description = "ID único del requerimiento", example = "105")
            @PathVariable Long id) {

        AuthenticatedClient client = clientService.getAuthenticatedClient();
        RequerimientoJudicialResponse response = requerimientoService.consultarRequerimiento(id);
        return ResponseEntity.ok(response);
    }
}
