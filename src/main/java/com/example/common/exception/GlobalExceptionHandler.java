package com.example.common.exception;

import jakarta.validation.ValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for all endpoints
 *
 * <p>
 * Converts exceptions to appropriate HTTP responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String STATUS = "status";
  private static final String ERROR = "error";
  private static final String MESSAGE = "message";

  /**
   * Handle validation exceptions (custom validators)
   *
   * <p>
   * When ValidationException is thrown → 400 Bad Request
   */
  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(Exception ex) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 400);
    response.put(ERROR, "Validation Failed");
    response.put(MESSAGE, ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handle Spring's built-in validation errors (@NotNull, @Email, etc.)
   *
   * <p>
   * When @Valid validation fails → 400 Bad Request with error details
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

    log.warn("Validation error in request body");

    // Extract all validation errors
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(
            Collectors.toMap(
                FieldError::getField,
                DefaultMessageSourceResolvable::getDefaultMessage,
                (existingMessage, newMessage) -> existingMessage + "; " + newMessage));

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 400);
    response.put(ERROR, "Validation Failed");
    response.put(MESSAGE, "Request body validation failed");
    response.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /** Handle cross-tenant access */
  @ExceptionHandler(CrossTenantAccessException.class)
  public ResponseEntity<Map<String, Object>> handleCrossTenantAccess(CrossTenantAccessException ex) {
    log.warn("Cross-tenant access attempt: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 403);
    response.put(ERROR, "Forbidden");
    response.put(MESSAGE, "Access denied: resource belongs to a different tenant");

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /** Handle resource not found (missing or wrong tenant) */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 404);
    response.put(ERROR, "Not Found");
    response.put(MESSAGE, ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /** Handle Access Denied (Spring Security) */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 403);
    response.put(ERROR, "Forbidden");
    response.put(MESSAGE, "You do not have permission to access this resource");

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /** Handle general exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    log.error("Unexpected error", ex);

    Map<String, Object> response = new HashMap<>();
    response.put(STATUS, 500);
    response.put(ERROR, "Internal Server Error");
    response.put(MESSAGE, "An unexpected error occurred");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
