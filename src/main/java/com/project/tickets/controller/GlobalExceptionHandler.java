package com.project.tickets.controller;

import com.project.tickets.domain.dtos.ErrorDto;
import com.project.tickets.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //various edge cases/states possible handle that
    //user not found , general exception , argumnet violation , constraint violation exception
    //make custom exception in eception and handle it here.

    //-----------------------------------------------------------------------------------------------
    //General Exception

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("An unknown error occurred");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //-----------------------------------------------------------------------------------------------
    //update exceptions


    @ExceptionHandler(EventUpdateException.class)
    public ResponseEntity<ErrorDto> handleEventUpdateExceptionException(EventUpdateException ex) {
        log.error("Caught EventUpdateException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("unable to update event");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketTypeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEventNotFoundException(TicketTypeNotFoundException ex) {
        log.error("Caught TicketTypeNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Ticket type not found");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEventNotFoundException(EventNotFoundException ex) {
        log.error("Caught EventUpdateException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Event not found");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //-------------------------------------------------------------------------------------------------

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("Caught UserNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("User not found");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("Caught MethodArgumentNotValidException", ex);
        ErrorDto errorDto = new ErrorDto();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Validation error occurred");

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        log.error("Caught ConstraintViolationException", ex);
        ErrorDto errorDto = new ErrorDto();

        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation ->
                        violation.getPropertyPath() + ": " + violation.getMessage()
                ).orElse("Constraint violation occurred");

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    //--------------------------------------------------------------------------QR Code Custom Exception

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<ErrorDto> handleQrCodeGenerationException(QrCodeGenerationException ex) {
        log.error("Caught QrCodeGenerationException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("unable to create QR code");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(QrCodeNotFoundException.class)
    public ResponseEntity<ErrorDto> handleQrCodeNotFoundException(QrCodeNotFoundException ex) {
        log.error("Caught QrCodeNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("QR Code not found");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TicketsSoldOutException.class)
    public ResponseEntity<ErrorDto> handleTicketsSoldOutException(TicketsSoldOutException ex) {
        log.error("Caught TicketsSoldOutException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("Tickets are not available");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }
    //-----------------------------------------------------------------------Tikcet Validation

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTicketNotFoundException(TicketNotFoundException ex) {
        log.error("Caught TicketNotFoundException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError("TicketNotFoundException");
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

}
