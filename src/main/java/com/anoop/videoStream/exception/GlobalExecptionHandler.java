package com.anoop.videoStream.exception;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExecptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIoExecption(IOException ex) {

        return ResponseEntity.status(503).body("An I/O error occurred");
    }

    @ExceptionHandler(ChunkNotReadyException.class)
    public ResponseEntity<Map<String, Object>> handleChunkNotReady(
            ChunkNotReadyException exception) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "2")
                .body(
                        Map.of(
                                "error", "CHUNK_NOT_READY",
                                "message", exception.getMessage()));
    }

}
