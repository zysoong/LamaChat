package com.example.backend.exceptionhandler;

import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        // You can customize the error message and HTTP status code as needed
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("[Resource not found]: " + ex.getMessage());
    }

    @ExceptionHandler(MongoWriteException.class)
    public ResponseEntity<String> handleMongoWriteException(MongoWriteException ex) {
        // You can customize the error message and HTTP status code as needed
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("[Write data to Mongo failed, may be unique key already exists?]: " + ex.getMessage());
    }

}