package es.guardiacivil.diligencias.remision.controller;

import es.guardiacivil.diligencias.remision.dto.RemisionDocumentoResponse;
import es.guardiacivil.diligencias.remision.dto.SolicitudRemisionRequest;
import es.guardiacivil.diligencias.remision.service.RemisionDocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/documentos/{documentoId}/remisiones")
@RequiredArgsConstructor
public class RemisionDocumentoController {

    private final RemisionDocumentoService remisionService;

    @PostMapping
    @PreAuthorize("hasAuthority('REMITIR_DOCUMENTO')")
    public ResponseEntity<RemisionDocumentoResponse> solicitarRemision(
            @PathVariable Long documentoId,
            @Valid @RequestBody SolicitudRemisionRequest request
    ) {
        RemisionDocumentoResponse remision = remisionService.solicitarRemision(documentoId, request);
        URI location = URI.create("/api/remisiones/%d".formatted(remision.id()));
        return ResponseEntity.created(location).body(remision);
    }
}
