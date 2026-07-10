package com.anoop.videoStream.Model;

import java.nio.file.Path;

public class ChunkUploadTask {

    private final String fileName;
    private final int chunkIndex;
    private final Path path;
    private int retryCount = 0; // needed for priority queue ordering
    private String videoID;
    private int totalChunk;

    public ChunkUploadTask(String fileName, int chunkIndex, Path path, String videoID, int totalChunk) {
        this.fileName = fileName;
        this.chunkIndex = chunkIndex;
        this.path = path;
        this.videoID = videoID;
        this.totalChunk = totalChunk;
    }

    public String getFileName() {
        return fileName;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public Path getPath() {
        return path;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public int getTotalChunk() {
        return totalChunk;
    }

    public void setTotalChunk(int totalChunk) {
        this.totalChunk = totalChunk;
    }

    public void incrementRetry() {
        retryCount++;
    }
}