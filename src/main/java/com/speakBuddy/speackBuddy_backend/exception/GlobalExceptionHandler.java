package com.speakBuddy.speackBuddy_backend.exception;


import com.speakBuddy.speackBuddy_backend.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(), // 404
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(), // 409
                ex.getMessage(), // "El email ya está registrado"
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AlreadyLearningLanguageException.class)
    public ResponseEntity<ErrorResponseDTO> handleAlreadyLearningLanguage(AlreadyLearningLanguageException ex, WebRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Manejador generico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                ex.getMessage(),
                request.getDescription(false)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
