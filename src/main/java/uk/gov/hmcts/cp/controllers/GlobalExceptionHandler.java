package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final Tracer tracer;

    public GlobalExceptionHandler(final Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException exception) {
        final ErrorResponse error = ErrorResponse.builder()
                .error(String.valueOf(exception.getStatusCode().value()))
                .message(exception.getReason() != null ? exception.getReason() : exception.getMessage())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .traceId(Objects.requireNonNull(tracer.currentSpan()).context().traceId())
                .build();
        LOG.atInfo().log("ResponseStatusException occurred: {}", error.getMessage(), exception);

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(error);
    }
}
