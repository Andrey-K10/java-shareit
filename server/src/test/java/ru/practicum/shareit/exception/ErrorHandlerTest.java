package ru.practicum.shareit.exception;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFound_shouldReturnNotFoundStatus() {
        NotFoundException exception = new NotFoundException("Item not found");

        Map<String, String> response = errorHandler.handleNotFound(exception);

        assertNotNull(response);
        assertEquals("Item not found", response.get("error"));
    }

    @Test
    void handleForbidden_shouldReturnForbiddenStatus() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        Map<String, String> response = errorHandler.handleForbidden(exception);

        assertNotNull(response);
        assertEquals("Access denied", response.get("error"));
    }

    @Test
    void handleBadRequest_shouldReturnBadRequestStatus() {
        BadRequestException exception = new BadRequestException("Bad request");

        Map<String, String> response = errorHandler.handleBadRequest(exception);

        assertNotNull(response);
        assertEquals("Bad request", response.get("error"));
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestStatus() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        Map<String, String> response = errorHandler.handleIllegalArgument(exception);

        assertNotNull(response);
        assertEquals("Invalid argument", response.get("error"));
    }

    @Test
    void handleNoSuchElement_shouldReturnNotFound() {
        NoSuchElementException exception = new NoSuchElementException("Element missing");

        Map<String, String> response = errorHandler.handleNoSuchElement(exception);

        assertNotNull(response);
        assertEquals("Element missing", response.get("error"));
    }
}