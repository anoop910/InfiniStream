package com.anoop.videoStream.exception;


public class ChunkNotReadyException extends RuntimeException {

    public ChunkNotReadyException(String message) {
        super(message);
    }
}