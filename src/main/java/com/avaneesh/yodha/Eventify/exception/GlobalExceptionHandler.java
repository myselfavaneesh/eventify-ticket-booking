package com.avaneesh.yodha.Eventify.exception;

import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation exceptions for method arguments.
     *
     * This method is triggered when a method argument annotated with @Valid fails validation.
     * It extracts the first validation error message and returns a ResponseEntity with a
     * BAD_REQUEST status and an ApiResponse containing the error message.
     *
     * @param ex The MethodArgumentNotValidException that was thrown.
     * @return A ResponseEntity containing an ApiResponse with the validation error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");
        ApiResponse<String> response = new ApiResponse<>(false, errorMessage, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    /**
     * Handles general exceptions that are not specifically caught by other handlers.
     *
     * This method acts as a fallback for any unhandled exceptions, returning a
     * ResponseEntity with an INTERNAL_SERVER_ERROR status and an ApiResponse
     * containing the exception's message.
     *
     * @param ex The Exception that was thrown.
     * @return A ResponseEntity containing an ApiResponse with the general error message.
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
    ApiResponse<String> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handles exceptions when a requested resource is not found.
     *
     * This method is triggered when a {@link ResourceNotFoundException} is thrown,
     * returning a ResponseEntity with a NOT_FOUND status and an ApiResponse
     * containing the exception's message.
     *
     * @param ex The ResourceNotFoundException that was thrown.
     * @return A ResponseEntity containing an ApiResponse with the resource not found message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<String> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles exceptions when a Resource with the same Data already exists.
     *
     * This method is triggered when a {@link ResourceAlreadyExistsException} is thrown,
     * returning a ResponseEntity with a CONFLICT status and an ApiResponse
     * containing the exception's message.
     *
     * @param ex The ResourceAlreadyExistsException that was thrown.
     * @return A ResponseEntity containing an ApiResponse with the Resource already exists message.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistsException(ResourceAlreadyExistsException ex) {
        ApiResponse<String> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
