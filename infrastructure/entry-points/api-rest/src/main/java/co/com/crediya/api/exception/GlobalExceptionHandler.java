package co.com.crediya.api.exception;

import co.com.crediya.api.dto.ErrorResponse;
import co.com.crediya.model.exceptions.BusinessException;
import co.com.crediya.model.exceptions.DocumentAlreadyRegisteredException;
import co.com.crediya.model.exceptions.EmailAlreadyRegisteredException;
import co.com.crediya.model.exceptions.UserNotFoundException;
import co.com.crediya.model.exceptions.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

/**
 * Translates every exception into a controlled JSON response so API clients
 * never receive stack traces or unexpected messages.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Error de validacion en {}: {}", request.getRequestURI(), ex.getErrors());
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), ex.getErrors(), request);
    }

    @ExceptionHandler({EmailAlreadyRegisteredException.class, DocumentAlreadyRegisteredException.class})
    public ResponseEntity<ErrorResponse> handleAlreadyRegistered(BusinessException ex, HttpServletRequest request) {
        log.warn("Conflicto en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        log.warn("Regla de negocio incumplida en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getCode(), ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        log.warn("Cuerpo de la peticion invalido en {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
                "El cuerpo de la petición es inválido o algún campo tiene un formato incorrecto "
                        + "(salario_base debe ser numérico y fecha_nacimiento con formato yyyy-MM-dd)",
                List.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        log.warn("Parametro invalido en {}: {}={}", request.getRequestURI(), ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "El parámetro " + ex.getName() + " tiene un formato inválido", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            // Standard Spring MVC errors (404, 405, 415, ...) keep their status but use our response format.
            HttpStatusCode status = springError.getStatusCode();
            log.warn("Error de peticion en {}: {}", request.getRequestURI(), ex.getMessage());
            String reason = status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Request error";
            return build(status, "REQUEST_ERROR", reason, List.of(), request);
        }
        log.error("Error inesperado procesando {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado, por favor intente más tarde", List.of(), request);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatusCode status, String code, String message,
                                                       List<String> errors, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), code, message, errors,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
