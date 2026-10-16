package es.guardiacivil.diligencias.diligencia.service;

import es.guardiacivil.diligencias.usuario.service.UsuarioService;

import es.guardiacivil.diligencias.core.security.UnidadAccessService;
import es.guardiacivil.diligencias.diligencia.dto.DiligenciaDTO;
import es.guardiacivil.diligencias.diligencia.dto.DiligenciaCreateRequest;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.diligencia.entity.EstadoDiligencia;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.diligencia.repository.DiligenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiligenciaService {

    /** Regex del número SIGO. Formato: año(4)-codigoUnidad(6)-numExpediente(8). */
    static final String SIGO_REGEX = "^[0-9]{4}-[0-9]{6}-[0-9]{8}$";

    /** Transiciones de estado permitidas. */
    private static final Set<String> TRANSICIONES_VALIDAS = Set.of(
            "BORRADOR->ABIERTA",
            "BORRADOR->CANCELADA",
            "ABIERTA->EN_TRAMITACION",
            "ABIERTA->CANCELADA",
            "EN_TRAMITACION->PENDIENTE_REVISION",
            "EN_TRAMITACION->CANCELADA",
            "PENDIENTE_REVISION->EN_TRAMITACION",
            "PENDIENTE_REVISION->REMITIDA_JUZGADO",
            "PENDIENTE_REVISION->CANCELADA",
            "REMITIDA_JUZGADO->ARCHIVADA"
    );

    private final DiligenciaRepository diligenciaRepository;
    private final UsuarioService usuarioService;
    private final UnidadAccessService unidadAccessService;

    // ─────────────────────────────────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DiligenciaDTO> obtenerDiligenciasPorUnidad() {
        if (unidadAccessService.isSuperadmin()) {
            return diligenciaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        }

        String unitCode = unidadAccessService.getRequiredUnitCode();
        if (unitCode == null || unitCode.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "El token JWT no contiene una unidad válida.");
        }
        return diligenciaRepository.findAll().stream()
                .filter(d -> d.getUnidad() != null && d.getUnidad().getCodigo().equals(unitCode))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DiligenciaDTO> obtenerDiligenciasPorUnidadPaginado(Pageable pageable) {
        if (unidadAccessService.isSuperadmin()) {
            return diligenciaRepository.findAll(pageable).map(this::mapToDTO);
        }

        String unitCode = unidadAccessService.getRequiredUnitCode();
        if (unitCode == null || unitCode.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "El token JWT no contiene una unidad válida.");
        }

        return diligenciaRepository.findByUnidad_Codigo(unitCode, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public DiligenciaDTO obtenerDiligenciaDTO(Long id) {
        return mapToDTO(obtenerDiligenciaEntidad(id));
    }

    @Transactional(readOnly = true)
    public Diligencia obtenerDiligenciaEntidad(Long id) {
        Diligencia diligencia = diligenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la diligencia con ID: " + id));

        unidadAccessService.checkAccessToUnit(
                diligencia.getUnidad() != null
                        ? diligencia.getUnidad().getCodigo()
                        : null
        );

        return diligencia;
    }

    @Transactional(readOnly = true)
    public Diligencia obtenerDiligenciaEntidadPorNumero(String numeroExpediente) {
        Diligencia diligencia = diligenciaRepository.findByNumeroExpediente(numeroExpediente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Diligencia no encontrada"));

        unidadAccessService.checkAccessToUnit(
                diligencia.getUnidad() != null
                        ? diligencia.getUnidad().getCodigo()
                        : null
        );
        return diligencia;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Creación — estado inicial BORRADOR, sin número SIGO
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public DiligenciaDTO crearDiligencia(DiligenciaCreateRequest request) {
        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();

        Diligencia diligencia = new Diligencia();
        diligencia.setUnidad(usuario.getUnidad());
        diligencia.setAgenteInstructor(usuario);
        diligencia.setEstado(EstadoDiligencia.BORRADOR);
        diligencia.setDelitoPrincipal(request.getDelitoPrincipal());
        diligencia.setGravedadDelito(request.getGravedadDelito());
        diligencia.setResumen(request.getResumen());

        // Los campos de retención se calcularán cuando el expediente pase a ABIERTA.
        // Hasta entonces quedan a null.

        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Asignar número SIGO → dispara la transición a ABIERTA si procede
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public DiligenciaDTO asignarNumeroSigo(Long id, String numeroSigo) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);

        if (diligencia.getEstado() != EstadoDiligencia.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Solo se puede asignar el número SIGO a un expediente en estado BORRADOR");
        }

        if (!StringUtils.hasText(numeroSigo) || !numeroSigo.matches(SIGO_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El número SIGO debe tener el formato año(4)-codigoUnidad(6)-numExpediente(8). Ej.: 2026-000177-00023550");
        }

        if (diligenciaRepository.findByNumeroExpediente(numeroSigo).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un expediente con el número SIGO: " + numeroSigo);
        }

        diligencia.setNumeroExpediente(numeroSigo);

        // Transición automática a ABIERTA si los datos mínimos están completos
        if (StringUtils.hasText(diligencia.getDelitoPrincipal())
                && diligencia.getGravedadDelito() != null) {
            diligencia.setEstado(EstadoDiligencia.ABIERTA);
            diligencia.setAniosRetencionLegal(10);
            diligencia.setFechaExpiracionRetencion(LocalDate.now().plusYears(10));
        }

        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Actualización de datos
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public DiligenciaDTO actualizarDiligencia(Long id, DiligenciaCreateRequest request) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);

        diligencia.setDelitoPrincipal(request.getDelitoPrincipal());
        diligencia.setGravedadDelito(request.getGravedadDelito());
        diligencia.setResumen(request.getResumen());

        if (diligencia.getEstado() == EstadoDiligencia.BORRADOR
                && StringUtils.hasText(diligencia.getNumeroExpediente())
                && StringUtils.hasText(diligencia.getDelitoPrincipal())
                && diligencia.getGravedadDelito() != null) {
            diligencia.setEstado(EstadoDiligencia.ABIERTA);
            diligencia.setAniosRetencionLegal(10);
            diligencia.setFechaExpiracionRetencion(LocalDate.now().plusYears(10));
        }

        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cambio de estado con validación de transiciones
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public DiligenciaDTO cambiarEstado(Long id, EstadoDiligencia estadoDestino) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);
        EstadoDiligencia estadoOrigen = diligencia.getEstado();

        String transicion = estadoOrigen.name() + "->" + estadoDestino.name();
        if (!TRANSICIONES_VALIDAS.contains(transicion)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Transición de estado no permitida: " + transicion);
        }

        // BORRADOR → ABIERTA solo puede ocurrir via asignarNumeroSigo
        if (estadoDestino == EstadoDiligencia.ABIERTA
                && estadoOrigen == EstadoDiligencia.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Para pasar de BORRADOR a ABIERTA asigne primero el número SIGO mediante PATCH /{id}/numero-sigo");
        }

        diligencia.setEstado(estadoDestino);
        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Eliminación — bloqueada en estado BORRADOR (deben cancelarse)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public void eliminarDiligencia(Long id) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);

        if (diligencia.getEstado() == EstadoDiligencia.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Los expedientes en borrador no pueden eliminarse físicamente. Cámbielos a CANCELADA.");
        }

        diligenciaRepository.delete(diligencia);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapeado
    // ─────────────────────────────────────────────────────────────────────────

    private DiligenciaDTO mapToDTO(Diligencia d) {
        return DiligenciaDTO.builder()
                .id(d.getId())
                .numeroExpediente(d.getNumeroExpediente())
                .unidadId(d.getUnidad() != null ? d.getUnidad().getId() : null)
                .agenteInstructorId(d.getAgenteInstructor() != null ? d.getAgenteInstructor().getId() : null)
                .agenteSecretarioId(d.getAgenteSecretario() != null ? d.getAgenteSecretario().getId() : null)
                .estado(d.getEstado())
                .delitoPrincipal(d.getDelitoPrincipal())
                .gravedadDelito(d.getGravedadDelito())
                .aniosRetencionLegal(d.getAniosRetencionLegal())
                .fechaExpiracionRetencion(d.getFechaExpiracionRetencion())
                .resumen(d.getResumen())
                .build();
    }
}
