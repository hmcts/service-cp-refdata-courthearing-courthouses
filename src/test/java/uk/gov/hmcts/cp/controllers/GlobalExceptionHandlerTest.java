package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private static final String ERROR = "Error";
    public static final String URL = "Url";
    @Spy
    Tracer tracer = Tracer.NOOP;

    @InjectMocks
    GlobalExceptionHandler globalExceptionHandler;

    @Mock
    MethodArgumentTypeMismatchException mismatchException;

    @Test
    void error_response_should_handle_ok() {
        ResponseStatusException e = new ResponseStatusException(INTERNAL_SERVER_ERROR, ERROR);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResponseStatusException(e);
        assertErrorFields(response, INTERNAL_SERVER_ERROR, ERROR);
    }

    @Test
    void server_exception_should_handle_ok() {
        HttpServerErrorException e = new HttpServerErrorException(INTERNAL_SERVER_ERROR, ERROR);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleServerException(e);
        assertErrorFields(response, INTERNAL_SERVER_ERROR, "500 Error");
    }

    @Test
    void client_exception_should_handle_ok() {
        HttpClientErrorException e = new HttpClientErrorException(BAD_REQUEST, ERROR);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleClientException(e);
        assertErrorFields(response, BAD_REQUEST, "400 Error");
    }

    @Test
    void bad_request_should_handle_ok() {
        when(mismatchException.getMessage()).thenReturn("Bad request data type");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentTypeMismatch(mismatchException);
        assertErrorFields(response, BAD_REQUEST, "Bad request data type");
    }

    @Test
    void no_resource_found_exception_should_handle_ok() {
        NoResourceFoundException e = new NoResourceFoundException(GET, URL, "Path");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoResourceFoundException(e);
        assertErrorFields(response, NOT_FOUND, "No static resource Path for request 'Url'.");
    }

    @Test
    void no_handler_found_exception_should_handle_ok() {
        NoHandlerFoundException e = new NoHandlerFoundException("GET", URL, null);
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNoHandlerFoundException(e);
        assertErrorFields(response, NOT_FOUND, "No endpoint GET Url.");
    }

    @Test
    void generic_exception_should_handle_ok() {
        Exception e = new Exception("message");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(e);
        assertErrorFields(response, INTERNAL_SERVER_ERROR, "message");
    }

    private void assertErrorFields(ResponseEntity<ErrorResponse> errorResponse, HttpStatusCode httpStatusCode, String message) {
        assertThat(errorResponse.getStatusCode()).isEqualTo(httpStatusCode);
        assertThat(errorResponse.getBody().getMessage()).isEqualTo(message);
        assertThat(errorResponse.getBody().getTraceId()).isNotNull();
        assertThat(errorResponse.getBody().getTimestamp()).isNotNull();
    }
}
