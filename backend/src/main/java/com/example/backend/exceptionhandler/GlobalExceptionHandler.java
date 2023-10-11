package com.example.backend.exceptionhandler;

import com.example.backend.exception.IllegalAuthenticationException;
import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        // You can customize the error message and HTTP status code as needed
        return new ResponseEntity<>(new ErrorResponse("[Resource not found]: " + ex.getMessage()), HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(MongoWriteException.class)
    public ResponseEntity<ErrorResponse> handleMongoWriteException(MongoWriteException ex) {
        // You can customize the error message and HTTP status code as needed
        return new ResponseEntity<>(new ErrorResponse("[Write data to Mongo failed, may be unique key already exists?]: " + ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleIllegalAuthenticationException(IllegalAuthenticationException ex) {
        // You can customize the error message and HTTP status code as needed
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

}
