package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Tracer;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(final Exception e) {
        log.error("GlobalExceptionHandler handleNoResourceFoundException");
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(buildErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({
        ConstraintViolationException.class,
        MethodArgumentTypeMismatchException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(final Exception exception) {
        log.error("Validation failed: {}", exception.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(buildErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException responseStatusException) {
        log.error("GlobalExceptionHandler handleResponseStatusException", responseStatusException);
        final String errorMessage = responseStatusException.getReason() != null
            ? responseStatusException.getReason()
            : responseStatusException.getMessage();
        return ResponseEntity
            .status(responseStatusException.getStatusCode())
            .body(buildErrorResponse(errorMessage));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleClientException(final HttpClientErrorException e) {
        log.error("GlobalExceptionHandler handleClientException", e);
        return ResponseEntity
            .status(e.getStatusCode())
            .body(buildErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleServerException(final HttpServerErrorException e) {
        log.error("GlobalExceptionHandler handleServerException", e);
        return ResponseEntity
            .status(e.getStatusCode())
            .body(buildErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("GlobalExceptionHandler handleException", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildErrorResponse(e.getMessage()));
    }

    private ErrorResponse buildErrorResponse(final String message) {
        return ErrorResponse.builder()
            .message(message)
            .timestamp(Instant.now())
            .traceId(Objects.requireNonNull(tracer.currentSpan()).context().traceId())
            .build();
    }
}
