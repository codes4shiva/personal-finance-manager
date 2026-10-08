package com.shivanshu.personal_finance_manager.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ApiException handler preserves status and details")
    void testHandleApiException() {
        ApiException ex = ApiException.badRequest("Test error", List.of("Detail 1", "Detail 2"));
        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test error", response.getBody().message());
        assertEquals(2, response.getBody().details().size());
    }

    @Test
    @DisplayName("HttpMessageNotReadableException returns 400 Bad Request")
    void testHandleMessageNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed", new MockHttpInputMessage(new byte[0]));
        ResponseEntity<ErrorResponse> response = handler.handleMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request", response.getBody().message());
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException returns 405 Method Not Allowed")
    void testHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupportedException(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    @DisplayName("NoResourceFoundException returns 404 Not Found")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/nonexistent");
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().message());
    }

    @Test
    @DisplayName("DataIntegrityViolationException returns 409 Conflict")
    void testHandleDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint fail");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Database constraint violation", response.getBody().message());
    }

    @Test
    @DisplayName("Generic Exception returns 500 Internal Server Error")
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected runtime error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
