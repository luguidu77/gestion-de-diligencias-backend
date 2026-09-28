package es.guardiacivil.diligencias.diligencia.service;

import es.guardiacivil.diligencias.usuario.service.UsuarioService;

import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiligenciaService {

    private final DiligenciaRepository diligenciaRepository;
    private final UsuarioService usuarioService;
    private final AuthenticatedUserService usuarioActualService;

    @Transactional(readOnly = true)
    public List<DiligenciaDTO> obtenerDiligenciasPorUnidad() {
        if (usuarioActualService.isSuperadmin()) {
            return diligenciaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        }
        
        String unitCode = usuarioActualService.getUnitCode();
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
        if (usuarioActualService.isSuperadmin()) {
            return diligenciaRepository.findAll(pageable).map(this::mapToDTO);
        }

        String unitCode = usuarioActualService.getUnitCode();
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

        if (usuarioActualService.isSuperadmin()) {
            return diligencia;
        }

        String unitCode = usuarioActualService.getUnitCode();
        if (diligencia.getUnidad() == null || !diligencia.getUnidad().getCodigo().equals(unitCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la diligencia con ID: " + id);
        }

        return diligencia;
    }

    @Transactional
    public DiligenciaDTO crearDiligencia(DiligenciaCreateRequest request) {
        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();
        
        Diligencia diligencia = new Diligencia();
        diligencia.setUnidad(usuario.getUnidad());
        diligencia.setAgenteInstructor(usuario);
        diligencia.setEstado(EstadoDiligencia.ABIERTA);
        diligencia.setDelitoPrincipal(request.getDelitoPrincipal());
        diligencia.setGravedadDelito(request.getGravedadDelito());
        diligencia.setResumen(request.getResumen());
        diligencia.setAniosRetencionLegal(10);
        diligencia.setFechaExpiracionRetencion(LocalDate.now().plusYears(10));
        
        // Simular generación de número (en un caso real se usa la secuencia de BBDD)
        long count = diligenciaRepository.count() + 1;
        diligencia.setNumeroExpediente(Diligencia.generarNumeroExpedienteOficial(usuario.getUnidad().getCodunidad(), count));

        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    @Transactional
    public DiligenciaDTO actualizarDiligencia(Long id, DiligenciaCreateRequest request) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);
        
        diligencia.setDelitoPrincipal(request.getDelitoPrincipal());
        diligencia.setGravedadDelito(request.getGravedadDelito());
        diligencia.setResumen(request.getResumen());
        
        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    @Transactional
    public DiligenciaDTO cambiarEstado(Long id, EstadoDiligencia estado) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);
        diligencia.setEstado(estado);
        return mapToDTO(diligenciaRepository.save(diligencia));
    }

    @Transactional
    public void eliminarDiligencia(Long id) {
        Diligencia diligencia = obtenerDiligenciaEntidad(id);
        diligenciaRepository.delete(diligencia);
    }

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
