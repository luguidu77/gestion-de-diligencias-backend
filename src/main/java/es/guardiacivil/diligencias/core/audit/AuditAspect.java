package es.guardiacivil.diligencias.core.audit;

import tools.jackson.databind.ObjectMapper;
import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;
import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import es.guardiacivil.diligencias.core.audit.entity.AuditLog;
import es.guardiacivil.diligencias.core.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final AuthenticatedUserService authenticatedUserService;

    @Around("@annotation(auditableEvent)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditableEvent auditableEvent) throws Throwable {
        Object result = null;
        Throwable exceptionThrown = null;
        long startTime = System.currentTimeMillis();

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            exceptionThrown = t;
            throw t;
        } finally {
            try {
                long duration = System.currentTimeMillis() - startTime;
                saveAuditLog(joinPoint, auditableEvent, result, exceptionThrown, duration);
            } catch (Exception e) {
                // Registrar el fallo en logs del servidor pero NO interrumpir la ejecuciÃ³n de la app principal
                log.error("Fallo al guardar el log de auditorÃ­a para la acciÃ³n: {}", auditableEvent.action(), e);
            }
        }
    }

    private void saveAuditLog(ProceedingJoinPoint joinPoint, AuditableEvent auditableEvent, Object result, Throwable exception, long duration) {
        String usuarioTip = "SYSTEM";
        String usuarioNombre = "Sistema AutÃ³nomo";
        String unidadCodigo = "N/A";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                // Delegar en AuthenticatedUserService para obtener identidad normalizada.
                // No se leen claims del JWT directamente â€” el mapeo lo aplica el servicio.
                AuthenticatedUser identity = authenticatedUserService.getAuthenticatedUser();
                usuarioTip   = identity.username()   != null ? identity.username()   : "UNKNOWN";
                usuarioNombre = identity.displayName() != null ? identity.displayName() : usuarioTip;
                unidadCodigo = identity.unitCode()   != null ? identity.unitCode()   : "N/A";
            } catch (Exception e) {
                log.warn("No se pudo obtener la identidad del usuario para auditorÃ­a: {}", e.getMessage());
                if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                    usuarioTip = jwt.getSubject(); // Fallback al claim 'sub' estÃ¡ndar
                } else {
                    usuarioTip = authentication.getName();
                }
            }
        }

        // Construir JSON de detalles de la llamada
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("metodo", joinPoint.getSignature().toShortString());
        detailsMap.put("duracion_ms", duration);
        
        if (exception != null) {
            detailsMap.put("estado", "ERROR");
            detailsMap.put("error_mensaje", exception.getMessage());
            detailsMap.put("error_tipo", exception.getClass().getSimpleName());
        } else {
            detailsMap.put("estado", "EXITOSO");
        }

        // Serializar los parÃ¡metros de entrada del mÃ©todo
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();
        Map<String, Object> params = new HashMap<>();
        
        if (parameterNames != null && parameterValues != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                // Evitamos guardar contraseÃ±as o datos sensibles si los hubiera
                if (parameterNames[i].toLowerCase().contains("password") || parameterNames[i].toLowerCase().contains("token")) {
                    params.put(parameterNames[i], "[REDACTADO]");
                } else {
                    params.put(parameterNames[i], parameterValues[i]);
                }
            }
        }
        detailsMap.put("parametros_entrada", params);

        String detallesJson = "{}";
        try {
            detallesJson = objectMapper.writeValueAsString(detailsMap);
        } catch (Exception e) {
            log.error("Fallo al serializar detalles de auditorÃ­a a JSON", e);
        }

        // Identificar ID de la entidad afectada a partir de los parÃ¡metros o del resultado si es posible
        String entidadId = "N/A";
        if (params.containsKey("id")) {
            entidadId = String.valueOf(params.get("id"));
        } else if (result instanceof org.springframework.http.ResponseEntity<?> resp && resp.getBody() != null) {
            try {
                Object body = resp.getBody();
                java.lang.reflect.Method getIdMethod = body.getClass().getMethod("getId");
                Object idVal = getIdMethod.invoke(body);
                if (idVal != null) entidadId = String.valueOf(idVal);
            } catch (Exception ignored) {}
        }

        AuditLog auditLog = AuditLog.builder()
                .usuarioTip(usuarioTip != null ? usuarioTip : "UNKNOWN")
                .usuarioNombre(usuarioNombre)
                .unidadCodigo(unidadCodigo)
                .accion(auditableEvent.action())
                .entidad(auditableEvent.entity())
                .entidadId(entidadId)
                .detalles(detallesJson)
                .resultado(exception == null
                        ? "OK"
                        : exception instanceof org.springframework.security.access.AccessDeniedException
                        ? "FORBIDDEN"
                        : "ERROR")
                .build();

        auditLogRepository.save(auditLog);
        log.info("AuditorÃ­a registrada: {} por usuario {} (Unidad: {})", auditableEvent.action(), usuarioTip, unidadCodigo);
    }
}
