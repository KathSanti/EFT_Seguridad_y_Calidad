package com.duoc.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest mockRequest() {
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");
        return request;
    }

    @Test
    void handleNotFound_resourceNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Paciente no encontrado con id: 1"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("Paciente no encontrado");
    }

    @Test
    void handleNotFound_emptyResultDataAccess_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new EmptyResultDataAccessException(1), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleBadRequest_illegalArgument_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadRequest(new IllegalArgumentException("dato invalido"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("dato invalido");
    }

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("user", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleBadCredentials_returns401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("Credenciales invalidas"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleDisabled_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDisabled(new DisabledException("Cuenta deshabilitada"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleGeneric_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new RuntimeException("boom"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Ocurrió un error inesperado en el servidor");
    }
}
