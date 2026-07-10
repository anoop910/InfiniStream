package com.anoop.videoStream.exception;

import java.io.IOException;
import java.io.ObjectInputFilter.Status;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExecptionHandler {


   @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIoExecption(IOException ex){


        return ResponseEntity.status(503).body("An I/O error occurred");
    }
    
}
