package com.gdg.backend.presentation;

import com.gdg.backend.application.DuplicateException;
import com.gdg.backend.application.NicknameGenerationException;
import com.gdg.backend.application.NotFoundException;
import com.gdg.backend.domain.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(DuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidation(MethodArgumentNotValidException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();
    }

    @ExceptionHandler(NicknameGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleNicknameGenerationException(NicknameGenerationException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDomainException(DomainException exception) {
        return exception.getMessage();
    }
}
