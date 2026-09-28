package es.guardiacivil.diligencias.usuario.controller;

import es.guardiacivil.diligencias.usuario.dto.AuthenticatedUser;
import es.guardiacivil.diligencias.usuario.service.AuthenticatedUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Tag(name = "Identidad de Usuario", description = "Endpoint de diagnóstico de sesión e identidad del usuario autenticado")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MeController {

    private final AuthenticatedUserService authenticatedUserService;

    @Operation(
            summary = "Obtener perfil e identidad del usuario autenticado",
            description = "Devuelve la información extraída del token JWT del usuario actual: TIP, roles corporativos, unidad asignada, empleo e información de caducidad del token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Identidad del usuario recuperada correctamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, caducado o inválido")
    })
    @GetMapping("/me")
    public Map<String, Object> getMyInfo(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            AuthenticatedUser identity = authenticatedUserService.getAuthenticatedUser();

            response.put("username", identity.username() != null ? identity.username() : jwt.getSubject());
            response.put("roles", identity.roles());
            response.put("unidad", identity.unitCode() != null ? identity.unitCode() : "PJ-LUG-01");
            response.put("empleo", identity.rank() != null ? identity.rank() : "Guardia Civil");

            response.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "UNKNOWN");
            response.put("issuedAt", jwt.getIssuedAt());
            response.put("expiresAt", jwt.getExpiresAt());
        } else {
            response.put("username", authentication != null ? authentication.getName() : "ANONYMOUS");
            response.put("roles", Collections.emptyList());
            response.put("unidad", "DESCONOCIDA");
            response.put("empleo", "DESCONOCIDO");
        }

        return response;
    }
}
