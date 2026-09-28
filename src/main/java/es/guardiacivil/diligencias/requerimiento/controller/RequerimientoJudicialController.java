package es.guardiacivil.diligencias.requerimiento.controller;

import es.guardiacivil.diligencias.requerimiento.dto.CrearRequerimientoJudicialRequest;
import es.guardiacivil.diligencias.requerimiento.dto.RequerimientoJudicialResponse;
import es.guardiacivil.diligencias.requerimiento.service.RequerimientoJudicialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/diligencias/{numeroDiligencia}/requerimientos")
@RequiredArgsConstructor
public class RequerimientoJudicialController {

    private final RequerimientoJudicialService requerimientoService;

    @PostMapping
    @PreAuthorize("hasAuthority('REQUERIMIENTO_WRITE')")
    public ResponseEntity<RequerimientoJudicialResponse> crearRequerimiento(
            @PathVariable String numeroDiligencia,
            @Valid @RequestBody CrearRequerimientoJudicialRequest request
    ) {
        RequerimientoJudicialResponse req = requerimientoService.crearRequerimiento(request, numeroDiligencia);
        URI location = URI.create("/api/requerimientos/%d".formatted(req.id()));
        return ResponseEntity.created(location).body(req);
    }
}
