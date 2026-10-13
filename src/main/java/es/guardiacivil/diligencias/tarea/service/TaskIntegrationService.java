package es.guardiacivil.diligencias.tarea.service;

import es.guardiacivil.diligencias.core.config.Roles;
import es.guardiacivil.diligencias.core.security.UnidadAccessService;
import es.guardiacivil.diligencias.diligencia.entity.Diligencia;
import es.guardiacivil.diligencias.diligencia.service.DiligenciaService;
import es.guardiacivil.diligencias.tarea.dto.SolicitudTareaRequest;
import es.guardiacivil.diligencias.tarea.dto.TareaResponse;
import es.guardiacivil.diligencias.tarea.port.TaskIntegrationPort;
import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;
import es.guardiacivil.diligencias.usuario.entity.Usuario;
import es.guardiacivil.diligencias.usuario.repository.UsuarioRepository;
import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskIntegrationService {

    private final TaskIntegrationPort taskPort;
    private final DiligenciaService diligenciaService;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final UnidadAccessService unidadAccessService;

    @Transactional
    public String notificarAsignacion(SolicitudTareaRequest request) {
        if (!StringUtils.hasText(request.numeroExpediente())
                || !StringUtils.hasText(request.tipAgente())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar el expediente y el TIP del agente");
        }

        Diligencia diligencia = diligenciaService
                .obtenerDiligenciaEntidadPorNumero(request.numeroExpediente());

        Usuario agente = obtenerAgente(request.tipAgente());
        comprobarUnidadAgente(agente);

        if (diligencia.getUnidad() == null
                || agente.getUnidad() == null
                || !Objects.equals(
                        diligencia.getUnidad().getId(),
                        agente.getUnidad().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La diligencia y el agente deben pertenecer a la misma unidad");
        }

        return taskPort.crearTarea(request);
    }

    @Transactional(readOnly = true)
    public void sincronizarEstado(String tareaExternaId, String nuevoEstado) {
        if (!StringUtils.hasText(nuevoEstado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debe indicar el nuevo estado");
        }

        TareaResponse tarea = taskPort.consultarTareaPorId(tareaExternaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tarea no encontrada"));

        // El expediente procede del servidor, no del cliente.
        diligenciaService.obtenerDiligenciaEntidadPorNumero(
                tarea.numeroExpediente());

        taskPort.actualizarEstadoTarea(tareaExternaId, nuevoEstado);
    }

    @Transactional(readOnly = true)
    public List<TareaResponse> obtenerTareasPorAgente(String tip) {
        AuthenticatedUser usuario =
                authenticatedUserService.getAuthenticatedUser();

        if (!usuario.isSuperadmin()) {
            unidadAccessService.getRequiredUnitCode();

            if (!tip.equalsIgnoreCase(usuario.username())
                    && !usuario.hasRole(Roles.ADMIN_UNIDAD)) {
                throw new AccessDeniedException(
                        "Solo puede consultar sus propias tareas");
            }

            comprobarUnidadAgente(obtenerAgente(tip));
        }

        List<TareaResponse> tareas = taskPort.consultarTareasPorAgente(tip);

        // También validar los expedientes de las tareas devueltas.
        // Una asignación externa no permite saltarse el aislamiento por unidad.
        for (TareaResponse tarea : tareas) {
            diligenciaService.obtenerDiligenciaEntidadPorNumero(
                    tarea.numeroExpediente());
        }

        return tareas;
    }

    private Usuario obtenerAgente(String tip) {
        return usuarioRepository.findByTip(tip.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agente no encontrado"));
    }

    private void comprobarUnidadAgente(Usuario agente) {
        unidadAccessService.checkAccessToUnit(
                agente.getUnidad() != null
                        ? agente.getUnidad().getCodigo()
                        : null
        );
    }
}
