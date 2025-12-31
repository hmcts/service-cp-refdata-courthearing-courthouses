package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(final Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
        final ResponseStatusException exception) {

        final ErrorResponse error = ErrorResponse.builder()
            .error(String.valueOf(exception.getStatusCode().value()))
            .message(exception.getReason() != null
                         ? exception.getReason()
                         : exception.getMessage())
            .timestamp(Instant.now())
            .traceId(Objects.requireNonNull(tracer.currentSpan())
                         .context()
                         .traceId())
            .build();

        log.info("ResponseStatusException occurred: {}", error.getMessage(), exception);

        return ResponseEntity
            .status(exception.getStatusCode())
            .body(error);
    }
}
