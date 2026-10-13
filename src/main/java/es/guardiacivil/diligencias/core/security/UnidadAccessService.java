package es.guardiacivil.diligencias.core.security;

import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * Control comun de aislamiento de recursos por unidad.
 * SUPERADMIN puede acceder a cualquier unidad.
 */
@Component
@RequiredArgsConstructor
public class UnidadAccessService {

    private final AuthenticatedUserService authenticatedUserService;

    public boolean isSuperadmin() {
        return authenticatedUserService.isSuperadmin();
    }

    public String getRequiredUnitCode() {
        String unitCode = authenticatedUserService.getUnitCode();
        if (!StringUtils.hasText(unitCode)) {
            throw new AccessDeniedException(
                    "El token JWT no contiene una unidad válida.");
        }
        return unitCode;
    }

    public void checkAccessToUnit(String resourceUnitCode) {
        if (isSuperadmin()) {
            return;
        }

        String authenticatedUnitCode = getRequiredUnitCode();
        if (!StringUtils.hasText(resourceUnitCode)
                || !Objects.equals(authenticatedUnitCode, resourceUnitCode)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Recurso no encontrado");
        }
    }
}
