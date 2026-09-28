package es.guardiacivil.diligencias.diligencia.service;

import es.guardiacivil.diligencias.usuario.service.UsuarioService;

import es.guardiacivil.diligencias.diligencia.dto.ActuacionDTO;
import es.guardiacivil.diligencias.diligencia.dto.ActuacionCreateRequest;
import es.guardiacivil.diligencias.diligencia.entity.Actuacion;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.diligencia.repository.ActuacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActuacionService {

    private final ActuacionRepository actuacionRepository;
    private final DiligenciaService diligenciaService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<ActuacionDTO> obtenerActuacionesPorDiligencia(Long diligenciaId) {
        // Valida que la diligencia pertenezca a la unidad del usuario
        diligenciaService.obtenerDiligenciaEntidad(diligenciaId);
        return actuacionRepository.findByDiligenciaIdOrderByNumeroOrdenAsc(diligenciaId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ActuacionDTO crearActuacion(Long diligenciaId, ActuacionCreateRequest request) {
        Diligencia diligencia = diligenciaService.obtenerDiligenciaEntidad(diligenciaId);
        Usuario usuario = usuarioService.obtenerUsuarioAutenticado();

        Actuacion ultima = actuacionRepository.findTopByDiligenciaIdOrderByNumeroOrdenDesc(diligenciaId);
        int numOrden = ultima != null ? ultima.getNumeroOrden() + 1 : 1;

        Actuacion actuacion = new Actuacion();
        actuacion.setDiligencia(diligencia);
        actuacion.setNumeroOrden(numOrden);
        actuacion.setTitulo(request.getTitulo());
        actuacion.setTipoActuacion(request.getTipoActuacion());
        actuacion.setLugar(request.getLugar());
        actuacion.setResumen(request.getResumen());
        actuacion.setUsuarioCreador(usuario);
        actuacion.setFechaActuacion(request.getFechaActuacion() != null ? request.getFechaActuacion() : java.time.LocalDateTime.now());

        return mapToDTO(actuacionRepository.save(actuacion));
    }

    @Transactional
    public ActuacionDTO actualizarActuacion(Long id, ActuacionCreateRequest request) {
        Actuacion actuacion = actuacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la actuación con ID: " + id));

        // Validar acceso a través del servicio de diligencias
        diligenciaService.obtenerDiligenciaEntidad(actuacion.getDiligencia().getId());

        actuacion.setTitulo(request.getTitulo());
        actuacion.setTipoActuacion(request.getTipoActuacion());
        actuacion.setLugar(request.getLugar());
        actuacion.setResumen(request.getResumen());
        if (request.getFechaActuacion() != null) {
            actuacion.setFechaActuacion(request.getFechaActuacion());
        }

        return mapToDTO(actuacionRepository.save(actuacion));
    }

    @Transactional
    public void eliminarActuacion(Long id) {
        Actuacion actuacion = actuacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la actuación con ID: " + id));
        
        diligenciaService.obtenerDiligenciaEntidad(actuacion.getDiligencia().getId());
        actuacionRepository.delete(actuacion);
    }

    private ActuacionDTO mapToDTO(Actuacion a) {
        return ActuacionDTO.builder()
                .id(a.getId())
                .diligenciaId(a.getDiligencia() != null ? a.getDiligencia().getId() : null)
                .numeroOrden(a.getNumeroOrden())
                .titulo(a.getTitulo())
                .tipoActuacion(a.getTipoActuacion())
                .lugar(a.getLugar())
                .resumen(a.getResumen())
                .usuarioCreadorId(a.getUsuarioCreador() != null ? a.getUsuarioCreador().getId() : null)
                .fechaActuacion(a.getFechaActuacion())
                .build();
    }
}
