package es.guardiacivil.diligencias.documento.controller;

import es.guardiacivil.diligencias.documento.dto.DocumentoDto;
import es.guardiacivil.diligencias.documento.dto.SubirDocumentoRequest;
import es.guardiacivil.diligencias.documento.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/diligencias/{diligenciaId}/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DILIGENCIA_WRITE')")
    public ResponseEntity<DocumentoDto> subir(
            @PathVariable Long diligenciaId,
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("datos") SubirDocumentoRequest request
    ) {
        DocumentoDto documento = documentoService.subirDocumento(diligenciaId, archivo, request);

        URI location = URI.create("/api/diligencias/%d/documentos/%d".formatted(diligenciaId, documento.id()));

        return ResponseEntity.created(location).body(documento);
    }
}
