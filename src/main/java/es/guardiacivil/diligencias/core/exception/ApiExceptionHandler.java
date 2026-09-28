package es.guardiacivil.diligencias.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Solicitud incorrecta");
        problem.setDetail(exception.getMessage());
        return problem;
    }

@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
public ProblemDetail accessDenied(org.springframework.security.access.AccessDeniedException exception) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Acceso denegado");
    problem.setDetail(exception.getMessage());
    return problem;
}
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ProblemDetail responseStatus(org.springframework.web.server.ResponseStatusException exception) {
        return exception.getBody();
    }
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail internalError(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error interno del servidor");
        problem.setDetail(exception.getMessage());
        return problem;
    }
}

